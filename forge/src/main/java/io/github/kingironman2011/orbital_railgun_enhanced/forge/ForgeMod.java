package io.github.kingironman2011.orbital_railgun_enhanced.forge;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.client.RailgunClient;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.*;
import net.minecraftforge.registries.DeferredRegister;

@Mod(OrbitalRailgun.MOD_ID)
public final class ForgeMod implements Platform {
  private static final SimpleChannel CHANNEL =
      ChannelBuilder.named(OrbitalRailgun.id("main")).networkProtocolVersion(4).simpleChannel();

  public ForgeMod(FMLJavaModLoadingContext context) {
    var bus = context.getModBusGroup();
    var items = DeferredRegister.create(Registries.ITEM, OrbitalRailgun.MOD_ID);
    var item = items.register("orbital_railgun", OrbitalRailgun::createItem);
    items.register(bus);
    var sounds = DeferredRegister.create(Registries.SOUND_EVENT, OrbitalRailgun.MOD_ID);
    sounds.register("railgun_shoot", () -> OrbitalRailgun.SHOOT);
    sounds.register("scope_on", () -> OrbitalRailgun.SCOPE);
    sounds.register("equip", () -> OrbitalRailgun.EQUIP);
    sounds.register(bus);
    BuildCreativeModeTabContentsEvent.BUS.addListener(
        e -> {
          if (e.getTabKey() == CreativeModeTabs.COMBAT) e.accept(item.get());
        });
    CHANNEL
        .messageBuilder(ShootPayload.class, 0, NetworkDirection.PLAY_TO_SERVER)
        .encoder((p, b) -> ShootPayload.CODEC.encode(b, p))
        .decoder(ShootPayload.CODEC::decode)
        .consumerMainThread(
            (p, c) -> {
              if (c.getSender() != null) StrikeManager.shoot(c.getSender());
            })
        .add();
    CHANNEL
        .messageBuilder(StrikePayload.class, 1, NetworkDirection.PLAY_TO_CLIENT)
        .encoder((p, b) -> StrikePayload.CODEC.encode(b, p))
        .decoder(StrikePayload.CODEC::decode)
        .consumerMainThread((p, c) -> RailgunClient.receive(p))
        .add();
    CHANNEL
        .messageBuilder(ServerSettings.Request.class, 2, NetworkDirection.PLAY_TO_SERVER)
        .encoder((p, b) -> ServerSettings.Request.CODEC.encode(b, p))
        .decoder(ServerSettings.Request.CODEC::decode)
        .consumerMainThread(
            (p, c) -> {
              if (c.getSender() != null) ServerSettings.receive(c.getSender(), p);
            })
        .add();
    CHANNEL
        .messageBuilder(ServerSettings.Snapshot.class, 3, NetworkDirection.PLAY_TO_CLIENT)
        .encoder((p, b) -> ServerSettings.Snapshot.CODEC.encode(b, p))
        .decoder(ServerSettings.Snapshot.CODEC::decode)
        .consumerMainThread(
            (p, c) ->
                io.github.kingironman2011.orbital_railgun_enhanced.client.ConfigScreen.receive(p))
        .add();
    CHANNEL.build();
    OrbitalRailgun.init(this);
  }

  public String loader() {
    return "Forge";
  }

  public void sendStrike(ServerPlayer player, StrikePayload payload) {
    CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
  }

  public void requestShot() {
    CHANNEL.send(ShootPayload.INSTANCE, PacketDistributor.SERVER.noArg());
  }

  public void requestSettings(ServerSettings.Request payload) {
    CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
  }

  public void sendSettings(ServerPlayer player, ServerSettings.Snapshot payload) {
    CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
  }
}
