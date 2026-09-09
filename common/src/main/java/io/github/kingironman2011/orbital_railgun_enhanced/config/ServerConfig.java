package io.github.kingironman2011.orbital_railgun_enhanced.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ServerConfig {
  private static final File CONFIG_FILE =
      new File("config/orbital-railgun-enhanced-server-config.json");
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  public static final ServerConfig INSTANCE = new ServerConfig();

  // Debug and logging
  private boolean debugMode = false;

  // Sound settings
  private double soundRange = 500.0;

  // Strike settings
  private float strikeDamage = 20.0f;
  private int cooldownTicks = 100;
  private int strikeRadius = 24;
  private int pullRadius = 24;

  public int getStrikeRadius() {
    return strikeRadius;
  }

  public int getPullRadius() {
    return pullRadius;
  }

  public void setStrikeRadius(int value) {
    strikeRadius = Math.clamp(value, 1, 64);
    saveConfig();
  }

  public void setPullRadius(int value) {
    pullRadius = Math.clamp(value, 0, 128);
    saveConfig();
  }

  // Performance settings
  private int maxActiveStrikes = 10;
  private boolean enableParticles = true;
  private boolean enablePull = true;
  private boolean destroyBedrock = true;

  public boolean isEnablePull() {
    return enablePull;
  }

  public boolean isDestroyBedrock() {
    return destroyBedrock;
  }

  public void setEnablePull(boolean value) {
    enablePull = value;
    saveConfig();
  }

  public void setDestroyBedrock(boolean value) {
    destroyBedrock = value;
    saveConfig();
  }

  public boolean isDebugMode() {
    return debugMode;
  }

  public double getSoundRange() {
    return soundRange;
  }

  public float getStrikeDamage() {
    return strikeDamage;
  }

  public int getCooldownTicks() {
    return cooldownTicks;
  }

  public int getMaxActiveStrikes() {
    return maxActiveStrikes;
  }

  public boolean isEnableParticles() {
    return enableParticles;
  }

  public void setDebugMode(boolean debugMode) {
    this.debugMode = debugMode;
    saveConfig();
    if (debugMode) {
      OrbitalRailgun.LOGGER.info("Debug mode enabled");
    } else {
      OrbitalRailgun.LOGGER.info("Debug mode disabled");
    }
  }

  public void setSoundRange(double soundRange) {
    this.soundRange = soundRange;
    saveConfig();
    if (isDebugMode()) {
      OrbitalRailgun.LOGGER.info("Sound range set to: {}", soundRange);
    }
  }

  public void setStrikeDamage(float strikeDamage) {
    this.strikeDamage = strikeDamage;
    saveConfig();
    if (isDebugMode()) {
      OrbitalRailgun.LOGGER.info("Strike damage set to: {}", strikeDamage);
    }
  }

  public void setCooldownTicks(int cooldownTicks) {
    this.cooldownTicks = cooldownTicks;
    saveConfig();
    if (isDebugMode()) {
      OrbitalRailgun.LOGGER.info("Cooldown ticks set to: {}", cooldownTicks);
    }
  }

  public void setMaxActiveStrikes(int maxActiveStrikes) {
    this.maxActiveStrikes = maxActiveStrikes;
    saveConfig();
    if (isDebugMode()) {
      OrbitalRailgun.LOGGER.info("Max active strikes set to: {}", maxActiveStrikes);
    }
  }

  public void setEnableParticles(boolean enableParticles) {
    this.enableParticles = enableParticles;
    saveConfig();
    if (isDebugMode()) {
      OrbitalRailgun.LOGGER.info("Particles enabled: {}", enableParticles);
    }
  }

  public void loadConfig() {
    if (CONFIG_FILE.exists()) {
      try (FileReader reader = new FileReader(CONFIG_FILE)) {
        ServerConfig config = GSON.fromJson(reader, ServerConfig.class);
        if (config != null) {
          this.debugMode = config.debugMode;
          this.soundRange =
              Double.isFinite(config.soundRange) ? Math.clamp(config.soundRange, 0, 2048) : 500;
          this.strikeDamage =
              Float.isFinite(config.strikeDamage) ? Math.clamp(config.strikeDamage, 0, 100000) : 20;
          this.cooldownTicks = Math.clamp(config.cooldownTicks, 0, 72000);
          this.strikeRadius = Math.clamp(config.strikeRadius, 1, 64);
          this.pullRadius = Math.clamp(config.pullRadius, 0, 128);
          this.maxActiveStrikes = Math.clamp(config.maxActiveStrikes, 1, 100);
          this.enableParticles = config.enableParticles;
          this.enablePull = config.enablePull;
          this.destroyBedrock = config.destroyBedrock;
          OrbitalRailgun.LOGGER.info(
              "Server configuration loaded from: {}", CONFIG_FILE.getAbsolutePath());
        } else {
          OrbitalRailgun.LOGGER.warn(
              "Config file parsed to null, using defaults: {}", CONFIG_FILE.getAbsolutePath());
        }
      } catch (IOException | com.google.gson.JsonParseException e) {
        OrbitalRailgun.LOGGER.error("Failed to load config: {}", e.getMessage());
      }
    } else {
      OrbitalRailgun.LOGGER.info(
          "Config file not found, creating default configuration: {}",
          CONFIG_FILE.getAbsolutePath());
      saveConfig();
    }
  }

  private void saveConfig() {
    try {
      File parentDir = CONFIG_FILE.getParentFile();
      if (parentDir != null && !parentDir.exists()) {
        boolean created = parentDir.mkdirs();
        if (!created && !parentDir.exists()) {
          OrbitalRailgun.LOGGER.warn(
              "Could not create config directory: {}", parentDir.getAbsolutePath());
        }
      }

      try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
        GSON.toJson(this, writer);
        OrbitalRailgun.LOGGER.info(
            "Server configuration saved to: {}", CONFIG_FILE.getAbsolutePath());
      }
    } catch (IOException | com.google.gson.JsonParseException e) {
      OrbitalRailgun.LOGGER.error("Failed to save config: {}", e.getMessage());
    }
  }
}
