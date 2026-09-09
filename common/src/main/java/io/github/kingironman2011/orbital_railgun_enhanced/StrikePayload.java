package io.github.kingironman2011.orbital_railgun_enhanced;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record StrikePayload(
    int id,
    BlockPos pos,
    Identifier dimension,
    int age,
    boolean stop,
    boolean particles,
    int radius)
    implements CustomPacketPayload {
  public StrikePayload(
      int id, BlockPos pos, Identifier dimension, int age, boolean stop, boolean particles) {
    this(id, pos, dimension, age, stop, particles, 24);
  }

  public static final Type<StrikePayload> TYPE = new Type<>(OrbitalRailgun.id("strike"));
  public static final StreamCodec<FriendlyByteBuf, StrikePayload> CODEC =
      new StreamCodec<>() {
        public StrikePayload decode(FriendlyByteBuf b) {
          return new StrikePayload(
              b.readVarInt(),
              b.readBlockPos(),
              b.readIdentifier(),
              b.readVarInt(),
              b.readBoolean(),
              b.readBoolean(),
              Math.clamp(b.readVarInt(), 1, 64));
        }

        public void encode(FriendlyByteBuf b, StrikePayload p) {
          b.writeVarInt(p.id);
          b.writeBlockPos(p.pos);
          b.writeIdentifier(p.dimension);
          b.writeVarInt(p.age);
          b.writeBoolean(p.stop);
          b.writeBoolean(p.particles);
          b.writeVarInt(p.radius);
        }
      };

  @Override
  public Type<StrikePayload> type() {
    return TYPE;
  }
}
