package io.github.kingironman2011.orbital_railgun_enhanced;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Empty intent packet: the server validates the held item and computes the target. */
public record ShootPayload() implements CustomPacketPayload {
  public static final ShootPayload INSTANCE = new ShootPayload();
  public static final Type<ShootPayload> TYPE = new Type<>(OrbitalRailgun.id("shoot"));
  public static final StreamCodec<FriendlyByteBuf, ShootPayload> CODEC = StreamCodec.unit(INSTANCE);

  @Override
  public Type<ShootPayload> type() {
    return TYPE;
  }
}
