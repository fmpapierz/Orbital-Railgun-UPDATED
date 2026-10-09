package io.github.kingironman2011.orbital_railgun_enhanced.quilt;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.RailgunClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class QuiltClient implements ClientModInitializer {
  public void onInitializeClient() {
    ClientPlayNetworking.registerGlobalReceiver(ServerSettings.Snapshot.TYPE,
        (payload, context) -> io.github.kingironman2011.orbital_railgun_enhanced.client.ConfigScreen.receive(payload));
    ClientPlayNetworking.registerGlobalReceiver(StrikePayload.TYPE,
        (payload, context) -> RailgunClient.receive(payload));
  }
}
