package io.github.kingironman2011.orbital_railgun_enhanced;

import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

/** Requests change one field. Snapshots and edit permissions always come from the server. */
public final class ServerSettings {
  public static final int READ = 0,
      PULL = 1,
      BEDROCK = 2,
      DEBUG = 3,
      SOUND_RANGE = 4,
      DAMAGE = 5,
      COOLDOWN = 6,
      MAX_STRIKES = 7,
      PARTICLES = 8,
      STRIKE_RADIUS = 9,
      PULL_RADIUS = 10;

  public record Request(int setting, double value) implements CustomPacketPayload {
    public Request(int setting, boolean value) {
      this(setting, value ? 1 : 0);
    }

    public static final Type<Request> TYPE = new Type<>(OrbitalRailgun.id("settings_request"));
    public static final StreamCodec<FriendlyByteBuf, Request> CODEC =
        new StreamCodec<>() {
          public Request decode(FriendlyByteBuf b) {
            return new Request(b.readVarInt(), b.readDouble());
          }

          public void encode(FriendlyByteBuf b, Request p) {
            b.writeVarInt(p.setting);
            b.writeDouble(p.value);
          }
        };

    public Type<Request> type() {
      return TYPE;
    }
  }

  public record Snapshot(
      boolean pull,
      boolean destroyBedrock,
      boolean editable,
      boolean debug,
      boolean particles,
      double soundRange,
      float damage,
      int cooldown,
      int maxStrikes,
      int strikeRadius,
      int pullRadius)
      implements CustomPacketPayload {
    public Snapshot(boolean pull, boolean bedrock, boolean editable) {
      this(pull, bedrock, editable, false, true, 500, 20, 100, 10, 24, 24);
    }

    public static final Type<Snapshot> TYPE = new Type<>(OrbitalRailgun.id("settings_snapshot"));
    public static final StreamCodec<FriendlyByteBuf, Snapshot> CODEC =
        new StreamCodec<>() {
          public Snapshot decode(FriendlyByteBuf b) {
            return new Snapshot(
                b.readBoolean(),
                b.readBoolean(),
                b.readBoolean(),
                b.readBoolean(),
                b.readBoolean(),
                b.readDouble(),
                b.readFloat(),
                b.readVarInt(),
                b.readVarInt(),
                b.readVarInt(),
                b.readVarInt());
          }

          public void encode(FriendlyByteBuf b, Snapshot p) {
            b.writeBoolean(p.pull);
            b.writeBoolean(p.destroyBedrock);
            b.writeBoolean(p.editable);
            b.writeBoolean(p.debug);
            b.writeBoolean(p.particles);
            b.writeDouble(p.soundRange);
            b.writeFloat(p.damage);
            b.writeVarInt(p.cooldown);
            b.writeVarInt(p.maxStrikes);
            b.writeVarInt(p.strikeRadius);
            b.writeVarInt(p.pullRadius);
          }
        };

    public Type<Snapshot> type() {
      return TYPE;
    }

    public double value(int setting) {
      return switch (setting) {
        case PULL -> pull ? 1 : 0;
        case BEDROCK -> destroyBedrock ? 1 : 0;
        case DEBUG -> debug ? 1 : 0;
        case PARTICLES -> particles ? 1 : 0;
        case SOUND_RANGE -> soundRange;
        case DAMAGE -> damage;
        case COOLDOWN -> cooldown;
        case MAX_STRIKES -> maxStrikes;
        case STRIKE_RADIUS -> strikeRadius;
        case PULL_RADIUS -> pullRadius;
        default -> 0;
      };
    }
  }

  public static boolean valid(int setting, double value) {
    if (!Double.isFinite(value)) return false;
    return switch (setting) {
      case READ -> true;
      case PULL, BEDROCK, DEBUG, PARTICLES -> value == 0 || value == 1;
      case SOUND_RANGE -> value >= 0 && value <= 2048;
      case DAMAGE -> value >= 0 && value <= 100000;
      case COOLDOWN -> integer(value, 0, 72000);
      case MAX_STRIKES -> integer(value, 1, 100);
      case STRIKE_RADIUS -> integer(value, 1, 64);
      case PULL_RADIUS -> integer(value, 0, 128);
      default -> false;
    };
  }

  private static boolean integer(double v, int min, int max) {
    return v >= min && v <= max && v == Math.rint(v);
  }

  public static boolean canEdit(ServerPlayer player) {
    return player
            .createCommandSourceStack()
            .permissions()
            .hasPermission(Permissions.COMMANDS_GAMEMASTER)
        || player
            .level()
            .getServer()
            .isSingleplayerOwner(
                new net.minecraft.server.players.NameAndId(player.getGameProfile()));
  }

  private static void send(ServerPlayer player) {
    var c = ServerConfig.INSTANCE;
    OrbitalRailgun.platform.sendSettings(
        player,
        new Snapshot(
            c.isEnablePull(),
            c.isDestroyBedrock(),
            canEdit(player),
            c.isDebugMode(),
            c.isEnableParticles(),
            c.getSoundRange(),
            c.getStrikeDamage(),
            c.getCooldownTicks(),
            c.getMaxActiveStrikes(),
            c.getStrikeRadius(),
            c.getPullRadius()));
  }

  public static void broadcast(net.minecraft.server.MinecraftServer server) {
    for (var player : server.getPlayerList().getPlayers()) send(player);
  }

  public static void receive(ServerPlayer player, Request request) {
    var c = ServerConfig.INSTANCE;
    if (request.setting != READ && canEdit(player) && valid(request.setting, request.value)) {
      double v = request.value;
      switch (request.setting) {
        case PULL -> c.setEnablePull(v == 1);
        case BEDROCK -> c.setDestroyBedrock(v == 1);
        case DEBUG -> c.setDebugMode(v == 1);
        case PARTICLES -> c.setEnableParticles(v == 1);
        case SOUND_RANGE -> c.setSoundRange(v);
        case DAMAGE -> c.setStrikeDamage((float) v);
        case COOLDOWN -> c.setCooldownTicks((int) v);
        case MAX_STRIKES -> c.setMaxActiveStrikes((int) v);
        case STRIKE_RADIUS -> c.setStrikeRadius((int) v);
        case PULL_RADIUS -> c.setPullRadius((int) v);
      }
      broadcast(player.level().getServer());
    } else send(player);
  }
}
