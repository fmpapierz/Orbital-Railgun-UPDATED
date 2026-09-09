package io.github.kingironman2011.orbital_railgun_enhanced.smoke;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.*;
import java.util.Set;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.*;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.*;

public final class ClientSmoke {
  private static boolean started, prepared, finished, sawStrike;
  private static volatile boolean impactVerified, worldVerified;
  private static int ticks, worldTicks;
  private static BlockPos impact;
  private static long start = System.currentTimeMillis();

  public static void input() {
    var mc = Minecraft.getInstance();
    if (prepared && !finished) {
      mc.options.keyUse.setDown(
          (worldTicks >= 70 && worldTicks < 105) || (worldTicks >= 1230 && worldTicks < 1245));
      mc.options.keyAttack.setDown(worldTicks >= 100 && worldTicks < 105);
    }
  }

  public static void tick() {
    var mc = Minecraft.getInstance();
    ticks++;
    if (finished) return;
    if (System.currentTimeMillis() - start > 240000)
      throw new IllegalStateException("ORE_SMOKE_TIMEOUT screen=" + mc.gui.screen());
    if (!started && mc.gui.screen() instanceof TitleScreen && mc.isGameLoadFinished()) {
      started = true;
      mc.options.pauseOnLostFocus = false;
      mc.options.renderDistance().set(6);
      mc.options.simulationDistance().set(5);
      var settings =
          new LevelSettings(
              "Orbital Railgun smoke",
              GameType.CREATIVE,
              new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false),
              true,
              WorldDataConfiguration.DEFAULT);
      mc.createWorldOpenFlows()
          .createFreshLevel(
              "ore-smoke-" + System.currentTimeMillis(),
              settings,
              new WorldOptions(262L, false, false),
              p ->
                  p.lookupOrThrow(Registries.WORLD_PRESET)
                      .getOrThrow(WorldPresets.FLAT)
                      .value()
                      .createWorldDimensions(),
              mc.gui.screen());
    }
    if (mc.level == null || mc.player == null) return;
    if (worldTicks < 1200 && mc.isPaused()) {
      if (mc.gui.screen() instanceof net.minecraft.client.gui.screens.PauseScreen)
        mc.gui.setScreen(null);
      return;
    }
    if (worldTicks == 1234 && !RangeShakeSmoke.ready()) return;
    if (worldTicks == 1249 && !RangeShakeSmoke.synchronizedShot()) return;
    worldTicks++;
    GameplaySmoke.tick(worldTicks, impact);
    ConfigSmoke.tick(worldTicks);
    RangeShakeSmoke.tick(worldTicks);
    if (!prepared) {
      prepared = true;
      var uuid = mc.player.getUUID();
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var server = mc.getSingleplayerServer();
                var p = server.getPlayerList().getPlayer(uuid);
                if (StrikeManager.shoot(p))
                  throw new IllegalStateException("Accepted shot without using a railgun");
                p.setGameMode(GameType.CREATIVE);
                p.getInventory()
                    .setItem(
                        0, new ItemStack(BuiltInRegistries.ITEM.getValue(OrbitalRailgun.ITEM_KEY)));
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
                p.teleportTo(p.level(), 0.5, -40, 0.5, Set.of(), 0, 25, true);
                p.level().getGameRules().set(GameRules.SPAWN_MOBS, false, server);
                // Raised surfaces make incorrect depth reconstruction visible in the projected
                // rings.
                for (int x : new int[] {-20, -10, 10, 20})
                  for (int z : new int[] {30, 45, 60})
                    for (int y = -60; y < -46; y++)
                      for (int dx = 0; dx < 2; dx++)
                        for (int dz = 0; dz < 2; dz++)
                          p.level()
                              .setBlock(
                                  new BlockPos(x + dx, y, z + dz),
                                  net.minecraft.world.level.block.Blocks.STONE_BRICKS
                                      .defaultBlockState(),
                                  2);
                if (server.getCommands().getDispatcher().getRoot().getChild("ore") == null)
                  throw new IllegalStateException("Commands missing");
                if (server
                    .getRecipeManager()
                    .byKey(
                        net.minecraft.resources.ResourceKey.create(
                            Registries.RECIPE, OrbitalRailgun.id("orbital_railgun")))
                    .isEmpty()) throw new IllegalStateException("Recipe missing");
                worldVerified = true;
                OrbitalRailgun.LOGGER.info(
                    "ORE_SMOKE_WORLD_LOADED loader={}", OrbitalRailgun.platform.loader());
              });
    }
    if (worldTicks == 70) {
      mc.gui.setScreen(null);
      mc.player.setXRot(25);
      mc.player.setYRot(0);
      mc.options.keyUse.setDown(true);
      var result = mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
      OrbitalRailgun.LOGGER.info(
          "ORE_SMOKE_USE result={} held={} using={} selected={}",
          result,
          mc.player.getMainHandItem(),
          mc.player.isUsingItem(),
          mc.player.getInventory().getSelectedSlot());
    }
    if (worldTicks == 90) {
      if (!RailgunClient.aiming() || RailgunClient.target == null)
        throw new IllegalStateException(
            "Aiming or target missing: held="
                + mc.player.getMainHandItem()
                + " using="
                + mc.player.isUsingItem()
                + " active="
                + mc.player.getUseItem()
                + " screen="
                + mc.gui.screen()
                + " target="
                + RailgunClient.target
                + " keyUse="
                + mc.options.keyUse.isDown());
      impact = RailgunClient.target.getBlockPos().immutable();
      photo("01-aim.png");
    }
    if (worldTicks == 60) photo("00-icon.png");
    if (worldTicks == 76) photo("01a-opening-line.png");
    if (worldTicks == 85) photo("01b-opening-expand.png");
    if (worldTicks == 99) photo("01c-aim-ring.png");
    if (worldTicks == 110) photo("02a-strike-cross.png");
    if (worldTicks == 145) photo("02b-strike-ring.png");
    if (worldTicks == 160) photo("02c-strike-transform.png");
    if (worldTicks == 100) mc.options.keyAttack.setDown(true);
    if (worldTicks == 105) {
      mc.options.keyAttack.setDown(false);
      mc.options.keyUse.setDown(false);
    }
    if (worldTicks == 130) {
      if (RailgunClient.EFFECTS.isEmpty())
        throw new IllegalStateException("Network strike was not received");
      sawStrike = true;
      OrbitalRailgun.LOGGER.info("ORE_SMOKE_SHOT_SYNCED target={}", impact);
      photo("02-strike.png");
    }
    if (worldTicks == 170) teleport(0.5, 600, 0.5);
    if (worldTicks == 200 && RailgunClient.EFFECTS.isEmpty())
      throw new IllegalStateException("Horizontal sound range incorrectly excludes high altitude");
    if (worldTicks == 205) teleport(1000.5, 600, 0.5);
    if (worldTicks == 240 && !RailgunClient.EFFECTS.isEmpty())
      throw new IllegalStateException("Effects did not stop outside sound range");
    if (worldTicks == 245) teleport(0.5, -40, 0.5);
    if (worldTicks == 285) {
      if (RailgunClient.EFFECTS.isEmpty()
          || RailgunClient.EFFECTS.values().iterator().next().age < 150)
        throw new IllegalStateException("Reentry did not restore strike age");
      OrbitalRailgun.LOGGER.info("ORE_SMOKE_RANGE_REENTRY_VERIFIED");
    }
    if (worldTicks == 320) ClientConfig.INSTANCE.enableShaderEffects = false;
    if (worldTicks == 330) photo("03-fallback.png");
    if (worldTicks == 360) ClientConfig.INSTANCE.enableShaderEffects = true;
    if (worldTicks == 430) photo("06-terrain-waves.png");
    if (worldTicks == 480) photo("07-terrain-waves.png");
    if (worldTicks == 750) photo("08-shrinking.png");
    if (worldTicks == 840) photo("09-beam-before-move.png");
    if (worldTicks == 845) teleport(30.5, -40, 0.5);
    if (worldTicks == 860) photo("10-beam-after-move.png");
    if (worldTicks == 870) {
      var server = mc.getSingleplayerServer();
      server.execute(
          () -> {
            var level = server.overworld();
            if (!level
                .getBlockState(new BlockPos(impact.getX(), level.getMinY(), impact.getZ()))
                .isAir()) throw new IllegalStateException("Bedrock was not cleared");
            if (level
                .getBlockState(new BlockPos(impact.getX() + 13, level.getMinY(), impact.getZ()))
                .isAir()) throw new IllegalStateException("Strike exceeded radius");
            impactVerified = true;
            OrbitalRailgun.LOGGER.info("ORE_SMOKE_IMPACT_VERIFIED");
            OrbitalRailgun.LOGGER.info("ORE_SMOKE_CONFIG_RADIUS_VERIFIED radius=12");
            var player = server.getPlayerList().getPlayer(mc.player.getUUID());
            player.setDeltaMovement(Vec3.ZERO);
            player.teleportTo(level, impact.getX(), -30, impact.getZ() - 65, Set.of(), 0, 35, true);
          });
      ClientConfig.INSTANCE.enableShaderEffects = false;
    }
    if (worldTicks == 900) photo("04-impact.png");
    if (worldTicks == 950) ClientConfig.INSTANCE.enableShaderEffects = true;
    if (worldTicks == 1060) photo("11-fade-start.png");
    if (worldTicks == 1110) photo("12-fade-half.png");
    if (worldTicks == 1155) photo("13-fade-end.png");
    if (worldTicks == 1170) photo("14-normal-lighting.png");
    if (worldTicks == 310) photo("15-gameplay-config.png");
    if (worldTicks == 1200) {
      GameplaySmoke.assertComplete();
      if (!worldVerified || !impactVerified || !sawStrike || !RailgunClient.EFFECTS.isEmpty())
        throw new IllegalStateException("Strike did not clean up");
      mc.gui.setScreen(new ConfigScreen(null));
    }
    if (worldTicks == 1210) photo("05-config.png");
    if (worldTicks == 1238) photo("16-render-distance-target.png");
    if (worldTicks == 1260) {
      finished = true;
      ClientConfig.INSTANCE.enableShaderEffects = true;
      ClientConfig.INSTANCE.save();
      OrbitalRailgun.LOGGER.info(
          "ORE_SMOKE_PASS loader={} worldLoaded=true shotSynced=true impactVerified=true",
          OrbitalRailgun.platform.loader());
      mc.stop();
    }
  }

  private static void teleport(double x, double y, double z) {
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    var id = mc.player.getUUID();
    server.execute(
        () -> {
          var p = server.getPlayerList().getPlayer(id);
          p.setDeltaMovement(Vec3.ZERO);
          p.teleportTo(p.level(), x, y, z, Set.of(), 0, 25, true);
        });
  }

  private static void photo(String name) {
    var mc = Minecraft.getInstance();
    Screenshot.grab(
        mc.gameDirectory,
        name,
        mc.gameRenderer.mainRenderTarget(),
        1,
        c -> OrbitalRailgun.LOGGER.info("ORE_SMOKE_SCREENSHOT {} {}", name, c.getString()));
  }
}
