package io.github.kingironman2011.orbital_railgun_enhanced.quilt;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;

public final class QuiltMod implements ModInitializer, Platform {
  public void onInitialize() {
    var item = Registry.register(BuiltInRegistries.ITEM, OrbitalRailgun.ITEM_KEY, OrbitalRailgun.createItem());
    Registry.register(BuiltInRegistries.SOUND_EVENT, OrbitalRailgun.SHOOT.location(), OrbitalRailgun.SHOOT);
    Registry.register(BuiltInRegistries.SOUND_EVENT, OrbitalRailgun.SCOPE.location(), OrbitalRailgun.SCOPE);
    Registry.register(BuiltInRegistries.SOUND_EVENT, OrbitalRailgun.EQUIP.location(), OrbitalRailgun.EQUIP);
    CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(entries -> entries.accept(item));
    PayloadTypeRegistry.serverboundPlay().register(ShootPayload.TYPE, ShootPayload.CODEC);
    PayloadTypeRegistry.clientboundPlay().register(StrikePayload.TYPE, StrikePayload.CODEC);
    PayloadTypeRegistry.serverboundPlay().register(ServerSettings.Request.TYPE, ServerSettings.Request.CODEC);
    PayloadTypeRegistry.clientboundPlay().register(ServerSettings.Snapshot.TYPE, ServerSettings.Snapshot.CODEC);
    ServerPlayNetworking.registerGlobalReceiver(ServerSettings.Request.TYPE,
        (payload, context) -> ServerSettings.receive(context.player(), payload));
    ServerPlayNetworking.registerGlobalReceiver(ShootPayload.TYPE,
        (payload, context) -> StrikeManager.shoot(context.player()));
    OrbitalRailgun.init(this);
  }

  public String loader() { return "Quilt"; }
  public void sendStrike(ServerPlayer player, StrikePayload payload) { ServerPlayNetworking.send(player, payload); }
  public void requestShot() { net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(ShootPayload.INSTANCE); }
  public void requestSettings(ServerSettings.Request payload) { net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(payload); }
  public void sendSettings(ServerPlayer player, ServerSettings.Snapshot payload) { ServerPlayNetworking.send(player, payload); }
}
