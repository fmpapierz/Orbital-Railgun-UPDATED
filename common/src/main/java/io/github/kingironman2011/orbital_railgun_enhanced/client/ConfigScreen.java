package io.github.kingironman2011.orbital_railgun_enhanced.client;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import io.github.kingironman2011.orbital_railgun_enhanced.ServerSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigScreen extends Screen {
  public static ServerSettings.Snapshot latest;
  private final Screen parent;
  private ServerSettings.Snapshot settings;
  private Button pullButton, bedrockButton;
  private boolean requested;

  public ConfigScreen(Screen parent) {
    super(Component.translatable("text.config.orbital-railgun-enhanced.title"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    var c = ClientConfig.INSTANCE;
    int index = 0;
    int buttonWidth = Math.min(210, (width - 36) / 2);
    for (String name :
        new String[] {
          "scopeVolume",
          "shootVolume",
          "equipVolume",
          "enableScopeSound",
          "enableShootSound",
          "enableEquipSound",
          "enableVisualEffects",
          "enableShaderEffects",
          "enableCameraShake",
          "cameraShakeIntensity"
        }) {
      int x = width / 2 + (index % 2 == 0 ? -buttonWidth - 4 : 4);
      int y = 32 + (index / 2) * 24;
      try {
        var field = ClientConfig.class.getField(name);
        var label = Component.translatable("text.config.orbital-railgun-enhanced.option." + name);
        if (field.getType() == double.class) {
          addRenderableWidget(
              new AbstractSliderButton(x, y, buttonWidth, 20, label, field.getDouble(c)) {
                {
                  updateMessage();
                }

                protected void updateMessage() {
                  setMessage(label.copy().append(": " + Math.round(value * 100) + "%"));
                }

                protected void applyValue() {
                  try {
                    field.setDouble(c, value);
                    c.save();
                  } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                  }
                }
              });
        } else {
          addRenderableWidget(
              Button.builder(
                      label.copy().append(": " + field.getBoolean(c)),
                      b -> {
                        try {
                          field.setBoolean(c, !field.getBoolean(c));
                          b.setMessage(label.copy().append(": " + field.getBoolean(c)));
                          c.save();
                        } catch (IllegalAccessException e) {
                          throw new IllegalStateException(e);
                        }
                      })
                  .bounds(x, y, buttonWidth, 20)
                  .build());
        }
      } catch (ReflectiveOperationException e) {
        throw new IllegalStateException(e);
      }
      index++;
    }
    int y = 32 + (index / 2) * 24;
    pullButton =
        addRenderableWidget(
            Button.builder(
                    Component.empty(),
                    b -> {
                      b.active = false;
                      OrbitalRailgun.platform.requestSettings(
                          new ServerSettings.Request(ServerSettings.PULL, !settings.pull()));
                    })
                .bounds(width / 2 - buttonWidth - 4, y, buttonWidth, 20)
                .build());
    bedrockButton =
        addRenderableWidget(
            Button.builder(
                    Component.empty(),
                    b -> {
                      b.active = false;
                      OrbitalRailgun.platform.requestSettings(
                          new ServerSettings.Request(
                              ServerSettings.BEDROCK, !settings.destroyBedrock()));
                    })
                .bounds(width / 2 + 4, y, buttonWidth, 20)
                .build());
    updateServerButtons();
    if (!requested && minecraft.getConnection() != null) {
      requested = true;
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.READ, false));
    }
    addRenderableWidget(
        Button.builder(Component.translatable("gui.done"), b -> onClose())
            .bounds(width / 2 + 4, y + 48, buttonWidth, 20)
            .build());
    addRenderableWidget(
        Button.builder(
                Component.translatable("ore.config.server"),
                b -> minecraft.gui.setScreen(new ServerConfigScreen(this)))
            .bounds(width / 2 - buttonWidth - 4, y + 48, buttonWidth, 20)
            .build());
  }

  @Override
  public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
    super.extractRenderState(g, x, y, delta);
    g.centeredText(font, title, width / 2, 15, 0xffffffff);
    g.centeredText(
        font,
        Component.translatable(
            "text.config.orbital-railgun-enhanced.server."
                + (settings == null ? "loading" : settings.editable() ? "shared" : "readonly")),
        width / 2,
        180,
        0xffaaaaaa);
  }

  public static void receive(ServerSettings.Snapshot snapshot) {
    latest = snapshot;
    if (Minecraft.getInstance().gui.screen() instanceof ServerConfigScreen serverScreen)
      serverScreen.receive(snapshot);
    if (Minecraft.getInstance().gui.screen() instanceof ConfigScreen screen) {
      screen.settings = snapshot;
      screen.updateServerButtons();
    }
  }

  private void updateServerButtons() {
    if (pullButton == null || bedrockButton == null) return;
    pullButton.setMessage(serverLabel("enablePull", settings != null && settings.pull()));
    bedrockButton.setMessage(
        serverLabel("destroyBedrock", settings != null && settings.destroyBedrock()));
    pullButton.active = bedrockButton.active = settings != null && settings.editable();
  }

  private Component serverLabel(String name, boolean value) {
    return Component.translatable("text.config.orbital-railgun-enhanced.option." + name)
        .append(": ")
        .append(
            settings == null
                ? Component.literal("...")
                : Component.translatable(value ? "options.on" : "options.off"));
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void onClose() {
    ClientConfig.INSTANCE.save();
    minecraft.gui.setScreen(parent);
  }
}
