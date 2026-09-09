package io.github.kingironman2011.orbital_railgun_enhanced.smoke;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.*;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;

/** Exercise the actual numeric edit boxes, Apply buttons and server acknowledgements. */
public final class ConfigSmoke {
  private static ServerSettings.Snapshot original;
  private static int originalRadius;
  private static final int[] IDS = {5, 4, 9, 10, 6, 7};
  private static final double[] VALUES = {7.5, 321.5, 12, 18, 432, 3};

  @SuppressWarnings("unchecked")
  private static <T> Map<Integer, T> controls(ServerConfigScreen screen, String name) {
    try {
      var f = ServerConfigScreen.class.getDeclaredField(name);
      f.setAccessible(true);
      return (Map<Integer, T>) f.get(screen);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void tick(int tick) {
    var mc = Minecraft.getInstance();
    if (tick == 58) {
      originalRadius = ConfigScreen.latest.strikeRadius();
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.STRIKE_RADIUS, 12));
    }
    if (tick == 135) {
      if (RailgunClient.EFFECTS.values().iterator().next().payload.radius() != 12)
        throw new IllegalStateException("Configured strike radius not synchronized");
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.STRIKE_RADIUS, originalRadius));
    }
    if (tick == 290) {
      original = ConfigScreen.latest;
      mc.gui.setScreen(new ServerConfigScreen(null));
    }
    if (tick == 296) {
      var screen = (ServerConfigScreen) mc.gui.screen();
      for (int i = 0; i < IDS.length; i++) {
        controls(screen, "fields").forEach((id, field) -> ((EditBox) field).setFocused(false));
        ConfigSmoke.<EditBox>controls(screen, "fields")
            .get(IDS[i])
            .setValue(Double.toString(VALUES[i]));
        var button = ConfigSmoke.<Button>controls(screen, "apply").get(IDS[i]);
        if (!button.active) throw new IllegalStateException("Numeric setting disabled: " + IDS[i]);
        button.onPress(null);
      }
      ConfigSmoke.<Button>controls(screen, "toggles").get(ServerSettings.DEBUG).onPress(null);
      ConfigSmoke.<Button>controls(screen, "toggles").get(ServerSettings.PARTICLES).onPress(null);
    }
    if (tick == 307) {
      for (int i = 0; i < IDS.length; i++)
        if (ConfigScreen.latest.value(IDS[i]) != VALUES[i])
          throw new IllegalStateException("Numeric setting not acknowledged: " + IDS[i]);
      if (ConfigScreen.latest.debug() == original.debug()
          || ConfigScreen.latest.particles() == original.particles())
        throw new IllegalStateException("Boolean setting not acknowledged");
      OrbitalRailgun.LOGGER.info("ORE_SMOKE_CONFIG_ALL_VERIFIED");
    }
    if (tick == 312) {
      for (int id : IDS)
        OrbitalRailgun.platform.requestSettings(new ServerSettings.Request(id, original.value(id)));
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.DEBUG, original.debug()));
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.PARTICLES, original.particles()));
    }
    if (tick == 318) {
      for (int id : IDS)
        if (ConfigScreen.latest.value(id) != original.value(id))
          throw new IllegalStateException("Setting not restored");
      mc.gui.setScreen(null);
    }
  }
}
