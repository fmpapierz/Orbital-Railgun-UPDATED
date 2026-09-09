package io.github.kingironman2011.orbital_railgun_enhanced.client;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Exact numeric inputs with explicit, independently acknowledged server updates. */
public final class ServerConfigScreen extends Screen {
  private final Screen parent;
  private ServerSettings.Snapshot settings;
  private final Map<Integer, EditBox> fields = new LinkedHashMap<>();
  private final Map<Integer, Button> apply = new LinkedHashMap<>(), toggles = new LinkedHashMap<>();
  private static final int[] NUMBERS = {
    ServerSettings.DAMAGE,
    ServerSettings.SOUND_RANGE,
    ServerSettings.STRIKE_RADIUS,
    ServerSettings.PULL_RADIUS,
    ServerSettings.COOLDOWN,
    ServerSettings.MAX_STRIKES
  };
  private static final int[] BOOLEANS = {
    ServerSettings.PULL, ServerSettings.BEDROCK, ServerSettings.PARTICLES, ServerSettings.DEBUG
  };

  public ServerConfigScreen(Screen parent) {
    super(Component.translatable("ore.config.server"));
    this.parent = parent;
  }

  public static String key(int id) {
    return switch (id) {
      case ServerSettings.DAMAGE -> "damage";
      case ServerSettings.SOUND_RANGE -> "soundRange";
      case ServerSettings.STRIKE_RADIUS -> "strikeRadius";
      case ServerSettings.PULL_RADIUS -> "pullRadius";
      case ServerSettings.COOLDOWN -> "cooldown";
      case ServerSettings.MAX_STRIKES -> "maxStrikes";
      case ServerSettings.PULL -> "pull";
      case ServerSettings.BEDROCK -> "bedrock";
      case ServerSettings.PARTICLES -> "particles";
      case ServerSettings.DEBUG -> "debug";
      default -> "unknown";
    };
  }

  @Override
  protected void init() {
    fields.clear();
    apply.clear();
    toggles.clear();
    int w = Math.min(210, (width - 36) / 2);
    for (int i = 0; i < NUMBERS.length; i++) {
      int id = NUMBERS[i], x = width / 2 + (i % 2 == 0 ? -w - 4 : 4), y = 44 + (i / 2) * 36;
      var field =
          new EditBox(font, x, y, w - 51, 20, Component.translatable("ore.config." + key(id)));
      field.setMaxLength(24);
      field.setResponder(text -> validate(id));
      fields.put(id, addRenderableWidget(field));
      var button =
          Button.builder(
                  Component.translatable("ore.config.apply"),
                  b -> {
                    double value = parse(field.getValue());
                    if (settings != null
                        && settings.editable()
                        && ServerSettings.valid(id, value)) {
                      b.active = false;
                      field.setFocused(false);
                      OrbitalRailgun.platform.requestSettings(
                          new ServerSettings.Request(id, value));
                    }
                  })
              .bounds(x + w - 47, y, 47, 20)
              .build();
      apply.put(id, addRenderableWidget(button));
    }
    for (int i = 0; i < BOOLEANS.length; i++) {
      int id = BOOLEANS[i], x = width / 2 + (i % 2 == 0 ? -w - 4 : 4), y = 153 + (i / 2) * 24;
      toggles.put(
          id,
          addRenderableWidget(
              Button.builder(
                      Component.empty(),
                      b -> {
                        b.active = false;
                        OrbitalRailgun.platform.requestSettings(
                            new ServerSettings.Request(id, settings.value(id) == 0));
                      })
                  .bounds(x, y, w, 20)
                  .build()));
    }
    addRenderableWidget(
        Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(width / 2 - 75, height - 24, 150, 20)
            .build());
    receive(ConfigScreen.latest);
    if (minecraft.getConnection() != null)
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.READ, false));
  }

  private static double parse(String s) {
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException e) {
      return Double.NaN;
    }
  }

  private static String number(double value) {
    return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
  }

  private void validate(int id) {
    var field = fields.get(id);
    var button = apply.get(id);
    if (field == null || button == null) return;
    double value = parse(field.getValue());
    boolean valid = ServerSettings.valid(id, value);
    field.setTextColor(valid ? 0xffeeeeee : 0xffff6666);
    double storedValue = id == ServerSettings.DAMAGE ? (double) (float) value : value;
    button.active =
        settings != null && settings.editable() && valid && storedValue != settings.value(id);
  }

  public void receive(ServerSettings.Snapshot snapshot) {
    settings = snapshot;
    for (var entry : fields.entrySet()) {
      int id = entry.getKey();
      var field = entry.getValue();
      field.setEditable(settings != null && settings.editable());
      if (settings != null && !field.isFocused())
        field.setValue(
            id == ServerSettings.DAMAGE
                ? Float.toString(settings.damage())
                : number(settings.value(id)));
      validate(id);
    }
    for (var entry : toggles.entrySet()) {
      var b = entry.getValue();
      b.active = settings != null && settings.editable();
      b.setMessage(
          Component.translatable("ore.config." + key(entry.getKey()))
              .append(": ")
              .append(
                  settings == null
                      ? Component.literal("...")
                      : Component.translatable(
                          settings.value(entry.getKey()) == 1 ? "options.on" : "options.off")));
    }
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
    super.extractRenderState(g, mouseX, mouseY, delta);
    g.centeredText(font, title, width / 2, 12, 0xffffffff);
    for (var entry : fields.entrySet())
      g.text(
          font,
          Component.translatable("ore.config." + key(entry.getKey())),
          entry.getValue().getX(),
          entry.getValue().getY() - 10,
          0xffeeeeee);
    g.centeredText(
        font,
        Component.translatable(
            settings != null && settings.editable()
                ? "ore.config.serverHint"
                : "ore.config.readonly"),
        width / 2,
        height - 37,
        0xffaaaaaa);
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void onClose() {
    minecraft.gui.setScreen(parent);
  }
}
