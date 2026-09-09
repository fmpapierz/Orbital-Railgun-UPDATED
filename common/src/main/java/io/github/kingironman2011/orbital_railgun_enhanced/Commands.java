package io.github.kingironman2011.orbital_railgun_enhanced;

import static net.minecraft.commands.Commands.*;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public final class Commands {
  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    var c = ServerConfig.INSTANCE;
    for (String alias : new String[] {"ore", "orbitalrailgun"}) {
      var root =
          literal(alias)
              .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
      root.executes(ctx -> help(ctx.getSource()))
          .then(literal("help").executes(ctx -> help(ctx.getSource())));
      root.then(
          option(
              "debug",
              BoolArgumentType.bool(),
              () -> c.isDebugMode(),
              v -> c.setDebugMode((Boolean) v)));
      root.then(
          option(
              "radius",
              DoubleArgumentType.doubleArg(0, 2048),
              () -> c.getSoundRange(),
              v -> c.setSoundRange((Double) v)));
      root.then(
          option(
              "strikeDamage",
              FloatArgumentType.floatArg(0, 100000),
              () -> c.getStrikeDamage(),
              v -> c.setStrikeDamage((Float) v)));
      root.then(
          option(
              "cooldown",
              IntegerArgumentType.integer(0, 72000),
              () -> c.getCooldownTicks(),
              v -> c.setCooldownTicks((Integer) v)));
      root.then(
          option(
              "maxStrikes",
              IntegerArgumentType.integer(1, 100),
              () -> c.getMaxActiveStrikes(),
              v -> c.setMaxActiveStrikes((Integer) v)));
      root.then(
          option(
              "particles",
              BoolArgumentType.bool(),
              () -> c.isEnableParticles(),
              v -> c.setEnableParticles((Boolean) v)));
      root.then(
          option(
              "pull",
              BoolArgumentType.bool(),
              () -> c.isEnablePull(),
              v -> c.setEnablePull((Boolean) v)));
      root.then(
          option(
              "bedrock",
              BoolArgumentType.bool(),
              () -> c.isDestroyBedrock(),
              v -> c.setDestroyBedrock((Boolean) v)));
      root.then(
          option(
              "strikeRadius",
              IntegerArgumentType.integer(1, 64),
              c::getStrikeRadius,
              v -> c.setStrikeRadius((Integer) v)));
      root.then(
          option(
              "pullRadius",
              IntegerArgumentType.integer(0, 128),
              c::getPullRadius,
              v -> c.setPullRadius((Integer) v)));
      root.then(
          literal("reload")
              .executes(
                  ctx -> {
                    c.loadConfig();
                    ServerSettings.broadcast(ctx.getSource().getServer());
                    ctx.getSource()
                        .sendSuccess(
                            () ->
                                Component.translatable(
                                    "command.orbital_railgun_enhanced.config.reloaded"),
                            true);
                    return 1;
                  }));
      dispatcher.register(root);
    }
  }

  private static int help(CommandSourceStack s) {
    s.sendSuccess(
        () ->
            Component.translatable("command.orbital_railgun_enhanced.help")
                .append("\n")
                .append(
                    Component.translatable("command.orbital_railgun_enhanced.help.worldSettings")),
        false);
    return 1;
  }

  private static LiteralArgumentBuilder<CommandSourceStack> option(
      String name,
      ArgumentType<?> type,
      java.util.function.Supplier<Object> get,
      java.util.function.Consumer<Object> set) {
    return literal(name)
        .executes(
            ctx -> {
              ctx.getSource()
                  .sendSuccess(
                      () ->
                          Component.translatable(
                              "command.orbital_railgun_enhanced." + name + ".current", get.get()),
                      false);
              return 1;
            })
        .then(
            argument("value", type)
                .executes(
                    ctx -> {
                      Object value = ctx.getArgument("value", Object.class);
                      set.accept(value);
                      ServerSettings.broadcast(ctx.getSource().getServer());
                      ctx.getSource()
                          .sendSuccess(
                              () ->
                                  Component.translatable(
                                      "command.orbital_railgun_enhanced." + name + ".set", value),
                              true);
                      return 1;
                    }));
  }
}
