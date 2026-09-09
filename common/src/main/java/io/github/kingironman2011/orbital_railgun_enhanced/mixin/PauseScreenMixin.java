package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.client.ConfigScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
  protected PauseScreenMixin(Component title) {
    super(title);
  }

  @Inject(method = "init", at = @At("TAIL"))
  private void ore$config(CallbackInfo ci) {
    addRenderableWidget(
        Button.builder(
                Component.literal("Railgun settings"),
                b -> minecraft.gui.setScreen(new ConfigScreen(this)))
            .bounds(5, height - 25, 130, 20)
            .build());
  }
}
