package io.github.kingironman2011.orbital_railgun_enhanced.smoke;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.ConfigScreen;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Regression checks for the server controls and the actual pull/destruction paths. */
public final class GameplaySmoke {
  private static volatile double probeX;
  private static volatile int pullChecks;
  private static volatile boolean protectedBedrock, destroyedBedrock;
  private static boolean settingsVerified;
  private static ServerSettings.Snapshot initial;

  public static void tick(int tick, BlockPos impact) {
    var mc = Minecraft.getInstance();
    if (tick == 20) {
      initial = ConfigScreen.latest;
      request(ServerSettings.PULL, true);
      request(ServerSettings.BEDROCK, true);
    }
    if (tick == 1180 && initial != null) {
      request(ServerSettings.PULL, initial.pull());
      request(ServerSettings.BEDROCK, initial.destroyBedrock());
    }
    if (tick == 25) mc.gui.setScreen(new ConfigScreen(null));
    if (tick == 35) {
      if (!snapshot().editable()) throw new IllegalStateException("Host cannot edit settings");
      click("Enable pull");
    }
    if (tick == 40) {
      if (snapshot().pull()) throw new IllegalStateException("Pull OFF not acknowledged");
      click("Enable pull");
      click("Destroy bedrock");
    }
    if (tick == 46) {
      if (!snapshot().pull() || snapshot().destroyBedrock())
        throw new IllegalStateException("Settings ACK incorrect: " + snapshot());
      click("Destroy bedrock");
    }
    if (tick == 54) {
      if (!snapshot().destroyBedrock())
        throw new IllegalStateException("Bedrock ON not acknowledged");
      settingsVerified = true;
      OrbitalRailgun.LOGGER.info("ORE_SMOKE_SETTINGS_VERIFIED");
    }
    if (tick == 56) mc.gui.setScreen(null);
    if (tick == 91)
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var level = mc.getSingleplayerServer().overworld();
                for (int y : new int[] {0, 80}) {
                  level.setBlock(
                      new BlockPos(impact.getX(), y, impact.getZ()),
                      Blocks.BEDROCK.defaultBlockState(),
                      2);
                  level.setBlock(
                      new BlockPos(impact.getX(), y + 1, impact.getZ()),
                      Blocks.STONE.defaultBlockState(),
                      2);
                }
              });
    if (tick == 520) probe(GameType.CREATIVE, 10, impact);
    if (tick == 545) verify(false, "creative");
    if (tick == 550) probe(GameType.SPECTATOR, 10, impact);
    if (tick == 575) verify(false, "spectator");
    if (tick == 580) probe(GameType.SURVIVAL, 80, impact);
    if (tick == 605) verify(false, "distant survival");
    if (tick == 610) probe(GameType.SURVIVAL, 10, impact);
    if (tick == 635) verify(true, "nearby survival");
    if (tick == 640) {
      probe(GameType.CREATIVE, 10, impact);
      request(ServerSettings.PULL, false);
    }
    if (tick == 646) probe(GameType.SURVIVAL, 10, impact);
    if (tick == 671) verify(false, "pull disabled");
    if (tick == 680) {
      probe(GameType.CREATIVE, 10, impact);
      request(ServerSettings.PULL, true);
    }
    if (tick == 685)
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var p = mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
                p.setDeltaMovement(Vec3.ZERO);
                p.teleportTo(p.level(), .5, -40, .5, Set.of(), 0, 25, true);
              });
    // The strike clears height in four-layer batches. Exercise both settings on real blocks.
    if (tick == 806) request(ServerSettings.BEDROCK, false);
    if (tick == 825)
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var level = mc.getSingleplayerServer().overworld();
                if (!level
                        .getBlockState(new BlockPos(impact.getX(), 0, impact.getZ()))
                        .is(Blocks.BEDROCK)
                    || !level.getBlockState(new BlockPos(impact.getX(), 1, impact.getZ())).isAir())
                  throw new IllegalStateException(
                      "Bedrock OFF must preserve bedrock and clear adjacent stone");
                protectedBedrock = true;
              });
    if (tick == 828) request(ServerSettings.BEDROCK, true);
    if (tick == 870)
      mc.getSingleplayerServer()
          .execute(
              () -> {
                var level = mc.getSingleplayerServer().overworld();
                if (!level.getBlockState(new BlockPos(impact.getX(), 80, impact.getZ())).isAir())
                  throw new IllegalStateException("Bedrock ON did not clear bedrock");
                destroyedBedrock = true;
                OrbitalRailgun.LOGGER.info("ORE_SMOKE_BEDROCK_TOGGLE_VERIFIED");
              });
  }

  private static void request(int setting, boolean value) {
    OrbitalRailgun.platform.requestSettings(new ServerSettings.Request(setting, value));
  }

  private static void probe(GameType mode, double distance, BlockPos impact) {
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    var id = mc.player.getUUID();
    server.execute(
        () -> {
          var p = server.getPlayerList().getPlayer(id);
          p.setGameMode(mode);
          p.setDeltaMovement(Vec3.ZERO);
          probeX = impact.getX() + .5 + distance;
          // Keep the probes clear of the visual test's stone pillars (z=45..46).
          p.teleportTo(p.level(), probeX, -60, impact.getZ() + 5.5, Set.of(), 0, 0, true);
          p.getAbilities().flying = mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
          p.onUpdateAbilities();
        });
  }

  private static void verify(boolean shouldMove, String label) {
    var mc = Minecraft.getInstance();
    var server = mc.getSingleplayerServer();
    var id = mc.player.getUUID();
    server.execute(
        () -> {
          double moved = probeX - server.getPlayerList().getPlayer(id).getX();
          if (shouldMove ? moved < .1 : Math.abs(moved) > .05)
            throw new IllegalStateException(
                "Pull check failed: " + label + " displacement=" + moved);
          pullChecks++;
          OrbitalRailgun.LOGGER.info("ORE_SMOKE_PULL_VERIFIED {} displacement={}", label, moved);
        });
  }

  private static ServerSettings.Snapshot snapshot() {
    try {
      var f = ConfigScreen.class.getDeclaredField("settings");
      f.setAccessible(true);
      var value = (ServerSettings.Snapshot) f.get(Minecraft.getInstance().gui.screen());
      if (value == null) throw new IllegalStateException("No settings snapshot");
      return value;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void click(String label) {
    for (var child : Minecraft.getInstance().gui.screen().children())
      if (child instanceof Button b && b.getMessage().getString().startsWith(label)) {
        if (!b.active) throw new IllegalStateException("Disabled control: " + label);
        b.onPress(null);
        return;
      }
    throw new IllegalStateException("Missing control: " + label);
  }

  public static void assertComplete() {
    if (!settingsVerified || pullChecks != 5 || !protectedBedrock || !destroyedBedrock)
      throw new IllegalStateException("Gameplay regressions did not all pass");
  }
}
