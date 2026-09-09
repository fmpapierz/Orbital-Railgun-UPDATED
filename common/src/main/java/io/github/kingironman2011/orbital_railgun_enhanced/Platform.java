package io.github.kingironman2011.orbital_railgun_enhanced;

import net.minecraft.server.level.ServerPlayer;

public interface Platform {
  String loader();

  void sendStrike(ServerPlayer player, StrikePayload payload);

  void requestShot();

  void requestSettings(ServerSettings.Request payload);

  void sendSettings(ServerPlayer player, ServerSettings.Snapshot payload);
}
