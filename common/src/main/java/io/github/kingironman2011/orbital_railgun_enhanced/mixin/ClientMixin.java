package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.client.RailgunClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class ClientMixin {
  @Inject(method = "tick", at = @At("TAIL"))
  private void ore$tick(CallbackInfo ci) {
    RailgunClient.tick();
  }

  @Inject(method = "handleKeybinds", at = @At("HEAD"))
  private void ore$input(CallbackInfo ci) {
    RailgunClient.input();
  }
}
