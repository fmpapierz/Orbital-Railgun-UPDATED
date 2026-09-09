package io.github.kingironman2011.orbital_railgun_enhanced.client;

import com.google.gson.*;
import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import java.io.*;
import java.nio.file.*;

public final class ClientConfig {
  private static final Path FILE = Path.of("config/orbital-railgun-enhanced.json5");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  public static final ClientConfig INSTANCE = load();
  public double scopeVolume = 1, shootVolume = 0.5, equipVolume = 1;
  public boolean enableScopeSound = true, enableShootSound = true, enableEquipSound = true;
  public boolean enableVisualEffects = true, enableShaderEffects = true;
  public boolean enableCameraShake = true;
  public double cameraShakeIntensity = 0.35;

  private static ClientConfig load() {
    ClientConfig c = new ClientConfig();
    if (Files.exists(FILE))
      try (var reader = Files.newBufferedReader(FILE)) {
        var parsed = GSON.fromJson(reader, ClientConfig.class);
        if (parsed != null) c = parsed;
      } catch (IOException | JsonParseException e) {
        OrbitalRailgun.LOGGER.warn("Cannot read client config; using defaults", e);
      }
    c.scopeVolume = volume(c.scopeVolume);
    c.shootVolume = volume(c.shootVolume);
    c.equipVolume = volume(c.equipVolume);
    c.cameraShakeIntensity = volume(c.cameraShakeIntensity);
    c.save();
    return c;
  }

  private static double volume(double v) {
    return Double.isFinite(v) ? Math.clamp(v, 0, 1) : 1;
  }

  public void save() {
    try {
      Files.createDirectories(FILE.getParent());
      try (var out = Files.newBufferedWriter(FILE)) {
        GSON.toJson(this, out);
      }
    } catch (IOException e) {
      OrbitalRailgun.LOGGER.error("Cannot save client config", e);
    }
  }
}
