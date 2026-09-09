package io.github.kingironman2011.orbital_railgun_enhanced.smoke;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.*;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix4f;

public final class RangeShakeSmoke {
  private static final BlockPos FAR = new BlockPos(0, -39, 480);
  private static ClientInformation oldInfo;
  private static boolean shook;
  private static int oldDistance;
  private static boolean oldDebug;
  private static volatile boolean prepared;

  public static boolean ready() {
    var mc = Minecraft.getInstance();
    return prepared
        && mc.player.position().distanceToSqr(new net.minecraft.world.phys.Vec3(.5, -40, .5)) < 1
        && mc.level.hasChunkAt(FAR)
        && mc.level.getBlockState(FAR).is(Blocks.STONE_BRICKS);
  }

  public static boolean synchronizedShot() {
    return RailgunClient.EFFECTS.values().stream().anyMatch(e -> e.payload.pos().equals(FAR));
  }

  public static void tick(int tick) {
    var mc = Minecraft.getInstance();
    if (tick >= 805 && tick <= 830 && !shook && !RailgunClient.EFFECTS.isEmpty()) {
      var effect = RailgunClient.EFFECTS.values().iterator().next();
      if (effect.age > 705 && effect.age < 755) {
        var c = ClientConfig.INSTANCE;
        boolean enabled = c.enableCameraShake;
        double intensity = c.cameraShakeIntensity;
        var position = mc.player.position();
        float yaw = mc.player.getYRot(), pitch = mc.player.getXRot();
        c.enableCameraShake = true;
        c.cameraShakeIntensity = 1;
        Matrix4f moving = new Matrix4f();
        CameraShake.apply(moving, .25f);
        c.enableCameraShake = false;
        Matrix4f disabled = new Matrix4f();
        CameraShake.apply(disabled, .25f);
        c.enableCameraShake = true;
        c.cameraShakeIntensity = 0;
        Matrix4f zero = new Matrix4f();
        CameraShake.apply(zero, .25f);
        c.enableCameraShake = enabled;
        c.cameraShakeIntensity = intensity;
        if (moving.equals(new Matrix4f())
            || !disabled.equals(new Matrix4f())
            || !zero.equals(new Matrix4f()))
          throw new IllegalStateException(
              "Camera shake intensity/toggle did not affect projection");
        if (!position.equals(mc.player.position())
            || yaw != mc.player.getYRot()
            || pitch != mc.player.getXRot())
          throw new IllegalStateException("Camera shake moved player or aiming rotation");
        shook = true;
        OrbitalRailgun.LOGGER.info("ORE_SMOKE_CAMERA_SHAKE_VERIFIED");
      }
    }
    if (tick == 1212) {
      if (!shook) throw new IllegalStateException("Impact camera shake not checked");
      mc.gui.setScreen(null);
      oldDistance = mc.options.renderDistance().get();
      mc.options.renderDistance().set(32);
      mc.options.broadcastOptions();
      mc.level.getChunkSource().updateViewRadius(32);
      var server = mc.getSingleplayerServer();
      var id = mc.player.getUUID();
      server.execute(
          () -> {
            var p = server.getPlayerList().getPlayer(id);
            oldDebug =
                io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig.INSTANCE
                    .isDebugMode();
            io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig.INSTANCE
                .setDebugMode(true);
            var level = p.level();
            level.setChunkForced(FAR.getX() >> 4, FAR.getZ() >> 4, true);
            p.teleportTo(level, .5, -40, .5, Set.of(), 0, 0, true);
            level.setBlock(FAR, Blocks.STONE_BRICKS.defaultBlockState(), 2);
            oldInfo = p.clientInformation();
            p.updateOptions(
                new ClientInformation(
                    oldInfo.language(),
                    32,
                    oldInfo.chatVisibility(),
                    oldInfo.chatColors(),
                    oldInfo.modelCustomisation(),
                    oldInfo.mainHand(),
                    oldInfo.textFilteringEnabled(),
                    oldInfo.allowsListing(),
                    oldInfo.particleStatus()));
            p.connection.send(
                new ClientboundLevelChunkWithLightPacket(
                    level.getChunkAt(FAR), level.getLightEngine(), null, null));
            var hit = Targeting.pick(p, p.requestedViewDistance());
            if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(FAR))
              throw new IllegalStateException("Server far target missing: " + hit.getBlockPos());
            p.startUsingItem(InteractionHand.MAIN_HAND);
            prepared = true;
          });
    }
    if (tick == 1235) {
      var hit = Targeting.pick(mc.player, 32);
      if (hit.getType() != HitResult.Type.BLOCK || !hit.getBlockPos().equals(FAR))
        throw new IllegalStateException("Client far target missing: " + hit.getBlockPos());
      if (Targeting.pick(mc.player, 6).getType() != HitResult.Type.MISS)
        throw new IllegalStateException("Target exceeded selected view distance");
      mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
    }
    if (tick == 1240) OrbitalRailgun.platform.requestShot();
    if (tick == 1250) {
      if (RailgunClient.EFFECTS.values().stream().noneMatch(e -> e.payload.pos().equals(FAR)))
        throw new IllegalStateException("Far shot not accepted or synchronized");
      var server = mc.getSingleplayerServer();
      var id = mc.player.getUUID();
      server.execute(
          () -> {
            server.overworld().setChunkForced(FAR.getX() >> 4, FAR.getZ() >> 4, false);
            server.getPlayerList().getPlayer(id).updateOptions(oldInfo);
            io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig.INSTANCE
                .setDebugMode(oldDebug);
          });
      mc.options.renderDistance().set(oldDistance);
      mc.options.broadcastOptions();
      mc.level.getChunkSource().updateViewRadius(mc.options.renderDistance().get());
      OrbitalRailgun.LOGGER.info(
          "ORE_SMOKE_RENDER_DISTANCE_TARGET_VERIFIED distance=480 chunks=32");
    }
  }
}
