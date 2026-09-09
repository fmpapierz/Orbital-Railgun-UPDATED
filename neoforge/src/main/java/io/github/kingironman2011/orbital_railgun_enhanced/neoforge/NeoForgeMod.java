package io.github.kingironman2011.orbital_railgun_enhanced.neoforge;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.RailgunClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(OrbitalRailgun.MOD_ID)
public final class NeoForgeMod implements Platform {
  public NeoForgeMod(IEventBus bus) {
    var items = DeferredRegister.create(Registries.ITEM, OrbitalRailgun.MOD_ID);
    var item = items.register("orbital_railgun", OrbitalRailgun::createItem);
    items.register(bus);
    var sounds = DeferredRegister.create(Registries.SOUND_EVENT, OrbitalRailgun.MOD_ID);
    sounds.register("railgun_shoot", () -> OrbitalRailgun.SHOOT);
    sounds.register("scope_on", () -> OrbitalRailgun.SCOPE);
    sounds.register("equip", () -> OrbitalRailgun.EQUIP);
    sounds.register(bus);
    bus.addListener(
        (BuildCreativeModeTabContentsEvent e) -> {
          if (e.getTabKey() == CreativeModeTabs.COMBAT) e.accept(item.get());
        });
    bus.addListener(
        (RegisterPayloadHandlersEvent e) -> {
          var r = e.registrar("4");
          r.playToServer(
              ServerSettings.Request.TYPE,
              ServerSettings.Request.CODEC,
              (p, c) -> ServerSettings.receive((ServerPlayer) c.player(), p));
          r.playToClient(
              ServerSettings.Snapshot.TYPE,
              ServerSettings.Snapshot.CODEC,
              (p, c) ->
                  io.github.kingironman2011.orbital_railgun_enhanced.client.ConfigScreen.receive(
                      p));
          r.playToServer(
              ShootPayload.TYPE,
              ShootPayload.CODEC,
              (p, c) -> StrikeManager.shoot((ServerPlayer) c.player()));
          r.playToClient(
              StrikePayload.TYPE, StrikePayload.CODEC, (p, c) -> RailgunClient.receive(p));
        });
    OrbitalRailgun.init(this);
  }

  public String loader() {
    return "NeoForge";
  }

  public void sendStrike(ServerPlayer player, StrikePayload payload) {
    PacketDistributor.sendToPlayer(player, payload);
  }

  public void requestShot() {
    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(
        ShootPayload.INSTANCE);
  }

  public void requestSettings(ServerSettings.Request payload) {
    net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
  }

  public void sendSettings(ServerPlayer player, ServerSettings.Snapshot payload) {
    PacketDistributor.sendToPlayer(player, payload);
  }
}
