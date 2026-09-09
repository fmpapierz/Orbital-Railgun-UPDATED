package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.smoke.ClientSmoke;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class SmokeMixin {
  @Inject(method = "tick", at = @At("HEAD"))
  private void ore$smokeInput(CallbackInfo ci) {
    ClientSmoke.input();
  }

  @Inject(method = "tick", at = @At("TAIL"))
  private void ore$smoke(CallbackInfo ci) {
    ClientSmoke.tick();
  }
}
