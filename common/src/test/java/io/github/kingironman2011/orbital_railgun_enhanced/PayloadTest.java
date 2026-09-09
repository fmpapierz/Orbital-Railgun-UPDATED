package io.github.kingironman2011.orbital_railgun_enhanced;

import static org.junit.jupiter.api.Assertions.*;

import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class PayloadTest {
  @Test
  void invalidNumericSettingsAreRejected() {
    for (int id = 0; id <= 10; id++) {
      assertFalse(ServerSettings.valid(id, Double.NaN));
      assertFalse(ServerSettings.valid(id, Double.POSITIVE_INFINITY));
      assertFalse(ServerSettings.valid(id, Double.NEGATIVE_INFINITY));
    }
    assertFalse(ServerSettings.valid(ServerSettings.STRIKE_RADIUS, 0));
    assertFalse(ServerSettings.valid(ServerSettings.STRIKE_RADIUS, 65));
    assertFalse(ServerSettings.valid(ServerSettings.COOLDOWN, 1.5));
    assertFalse(ServerSettings.valid(ServerSettings.MAX_STRIKES, 0));
    assertFalse(ServerSettings.valid(ServerSettings.PULL, 2));
    assertFalse(ServerSettings.valid(99, 1));
    assertTrue(ServerSettings.valid(ServerSettings.DAMAGE, 7.5));
    assertTrue(ServerSettings.valid(ServerSettings.PULL_RADIUS, 0));
  }

  @Test
  void settingsPacketsKeepIndependentFlagsAndServerPermission() {
    var buf = new FriendlyByteBuf(Unpooled.buffer());
    try {
      for (int setting :
          new int[] {ServerSettings.READ, ServerSettings.PULL, ServerSettings.BEDROCK}) {
        var request = new ServerSettings.Request(setting, false);
        ServerSettings.Request.CODEC.encode(buf, request);
        assertEquals(request, ServerSettings.Request.CODEC.decode(buf));
      }
      for (boolean pull : new boolean[] {false, true})
        for (boolean bedrock : new boolean[] {false, true})
          for (boolean editable : new boolean[] {false, true}) {
            var snapshot =
                new ServerSettings.Snapshot(
                    pull, bedrock, editable, true, false, 321.5, 7.5f, 432, 3, 12, 18);
            ServerSettings.Snapshot.CODEC.encode(buf, snapshot);
            assertEquals(snapshot, ServerSettings.Snapshot.CODEC.decode(buf));
          }
      assertEquals(0, buf.readableBytes());
    } finally {
      buf.release();
    }
  }

  @Test
  void strikeRoundTripRetainsNegativeCoordinatesDimensionAgeAndStop() {
    var original =
        new StrikePayload(
            17,
            new BlockPos(-300, -64, 29999900),
            Identifier.withDefaultNamespace("the_nether"),
            700,
            true,
            false,
            12);
    var buf = new FriendlyByteBuf(Unpooled.buffer());
    try {
      StrikePayload.CODEC.encode(buf, original);
      assertEquals(original, StrikePayload.CODEC.decode(buf));
      assertEquals(0, buf.readableBytes());
    } finally {
      buf.release();
    }
  }

  @Test
  void shootIntentCannotContainClientSuppliedItemOrTarget() {
    var buf = new FriendlyByteBuf(Unpooled.buffer());
    try {
      ShootPayload.CODEC.encode(buf, ShootPayload.INSTANCE);
      assertEquals(0, buf.readableBytes());
      assertEquals(ShootPayload.INSTANCE, ShootPayload.CODEC.decode(buf));
    } finally {
      buf.release();
    }
  }
}
