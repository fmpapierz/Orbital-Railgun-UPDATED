package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.client.RailgunClient;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
  @Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true)
  private void ore$hideVanillaScope(CallbackInfo ci) {
    if (RailgunClient.aiming()) ci.cancel();
  }

  @Inject(method = "extractRenderState", at = @At("TAIL"))
  private void ore$hud(GuiGraphicsExtractor g, DeltaTracker delta, CallbackInfo ci) {
    RailgunClient.hud(g);
  }
}
