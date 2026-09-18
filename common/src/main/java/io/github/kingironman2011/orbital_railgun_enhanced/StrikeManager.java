package io.github.kingironman2011.orbital_railgun_enhanced;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

/** All state is owned by the server thread and cleared on shutdown. */
public final class StrikeManager {
  private static final Map<MinecraftServer, List<Strike>> ACTIVE = new WeakHashMap<>();
  private static int nextId;

  private static final class Strike {
    final int id = ++nextId;
    final BlockPos pos;
    final ResourceKey<Level> dimension;
    final int start;
    final int radius;
    final Set<UUID> viewers = new HashSet<>();
    int clearY;
    boolean impacted;

    Strike(ServerLevel level, BlockPos pos, int start) {
      this.pos = pos.immutable();
      this.start = start;
      dimension = level.dimension();
      clearY = level.getMinY();
      radius = ServerConfig.INSTANCE.getStrikeRadius();
    }
  }

  public static boolean shoot(ServerPlayer player) {
    var stack = player.getUseItem();
    if (ServerConfig.INSTANCE.isDebugMode())
      OrbitalRailgun.LOGGER.info(
          "Shot request: using={} item={} cooldown={} viewDistance={}",
          player.isUsingItem(),
          stack,
          player.getCooldowns().isOnCooldown(stack),
          player.requestedViewDistance());
    if (!(stack.getItem() instanceof RailgunItem)
        || !player.isUsingItem()
        || player.isSpectator()
        || player.getCooldowns().isOnCooldown(stack)) return false;
    var block = Targeting.pick(player, player.requestedViewDistance());
    if (ServerConfig.INSTANCE.isDebugMode())
      OrbitalRailgun.LOGGER.info(
          "Shot target: type={} position={} player={} yaw={} pitch={}",
          block.getType(),
          block.getBlockPos(),
          player.position(),
          player.getYRot(),
          player.getXRot());
    if (block.getType() != HitResult.Type.BLOCK) return false;
    var level = player.level();
    if (!level.hasChunkAt(block.getBlockPos())
        || !level.getWorldBorder().isWithinBounds(block.getBlockPos())) return false;
    var strikes = ACTIVE.computeIfAbsent(level.getServer(), key -> new ArrayList<>());
    if (strikes.size() >= ServerConfig.INSTANCE.getMaxActiveStrikes()) return false;
    player.getCooldowns().addCooldown(stack, ServerConfig.INSTANCE.getCooldownTicks());
    player.stopUsingItem();
    strikes.add(new Strike(level, block.getBlockPos(), level.getServer().getTickCount()));
    if (ServerConfig.INSTANCE.isDebugMode())
      OrbitalRailgun.LOGGER.info(
          "Strike accepted from {} at {}", player.getName().getString(), block.getBlockPos());
    return true;
  }

  public static void tick(MinecraftServer server) {
    var strikes = ACTIVE.get(server);
    if (strikes == null) return;
    var config = ServerConfig.INSTANCE;
    for (var iterator = strikes.iterator(); iterator.hasNext(); ) {
      var s = iterator.next();
      var level = server.getLevel(s.dimension);
      int age = server.getTickCount() - s.start;
      if (level == null) {
        iterator.remove();
        continue;
      }
      for (var player : server.getPlayerList().getPlayers()) {
        double dx = player.getX() - (s.pos.getX() + 0.5), dz = player.getZ() - (s.pos.getZ() + 0.5);
        boolean inside =
            player.level() == level
                && dx * dx + dz * dz <= config.getSoundRange() * config.getSoundRange()
                && age < StrikeMath.END_TICK;
        boolean was = s.viewers.contains(player.getUUID());
        if (inside != was) {
          if (config.isDebugMode())
            OrbitalRailgun.LOGGER.info(
                "[SOUND] Player {} {} strike {} range",
                player.getName().getString(),
                inside ? "entered" : "exited",
                s.id);
          OrbitalRailgun.platform.sendStrike(
              player,
              new StrikePayload(
                  s.id,
                  s.pos,
                  s.dimension.identifier(),
                  age,
                  !inside,
                  config.isEnableParticles(),
                  s.radius));
          if (inside) s.viewers.add(player.getUUID());
          else s.viewers.remove(player.getUUID());
        }
      }
      s.viewers.removeIf(id -> server.getPlayerList().getPlayer(id) == null);
      if (config.isEnablePull() && age >= StrikeMath.PULL_TICK && age < StrikeMath.IMPACT_TICK) {
        for (var entity :
            level.getEntities(null, new AABB(s.pos).inflate(config.getPullRadius()))) {
          if (entity instanceof Player p && (p.isSpectator() || p.isCreative())) continue;
          Vec3 delta = Vec3.atCenterOf(s.pos).subtract(entity.position());
          if (delta.lengthSqr() > config.getPullRadius() * config.getPullRadius()) continue;
          entity.push(delta.normalize().scale(StrikeMath.pull(delta.length(), age)));
          entity.syncVelocity = true;
        }
      }
      if (age >= StrikeMath.IMPACT_TICK && !s.impacted) {
        var damageKey = ResourceKey.create(Registries.DAMAGE_TYPE, OrbitalRailgun.id("strike"));
        var damage =
            new DamageSource(
                level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(damageKey));
        for (var entity : level.getEntities(null, new AABB(s.pos).inflate(s.radius))) {
          if (entity.position().distanceToSqr(Vec3.atCenterOf(s.pos)) <= s.radius * s.radius)
            entity.hurtServer(level, damage, config.getStrikeDamage());
        }
        s.impacted = true;
        if (config.isDebugMode())
          OrbitalRailgun.LOGGER.info(
              "[STRIKE] Impact {} at {} in {}", s.id, s.pos, s.dimension.identifier());
      }
      // Bound block work per tick; clear the entire build height including bedrock.
      if (s.impacted && s.clearY <= level.getMaxY()) {
        int end = Math.min(s.clearY + 4, level.getMaxY() + 1);
        for (; s.clearY < end; s.clearY++)
          for (int x = -s.radius; x <= s.radius; x++)
            for (int z = -s.radius; z <= s.radius; z++) {
              if (!StrikeMath.inColumn(x, z, s.radius)) continue;
              var pos = new BlockPos(s.pos.getX() + x, s.clearY, s.pos.getZ() + z);
              var state = level.getBlockState(pos);
              if (!config.isDestroyBedrock() && state.is(Blocks.BEDROCK)) continue;
              if (!state.isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
      }
      if (age >= StrikeMath.END_TICK && s.clearY > level.getMaxY()) iterator.remove();
    }
  }

  public static void clear(MinecraftServer server) {
    ACTIVE.remove(server);
  }
}
