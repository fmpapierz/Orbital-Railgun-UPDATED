package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.StrikeManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
  @Inject(method = "tickServer", at = @At("TAIL"))
  private void ore$tick(CallbackInfo ci) {
    StrikeManager.tick((MinecraftServer) (Object) this);
  }

  @Inject(method = "stopServer", at = @At("HEAD"))
  private void ore$stop(CallbackInfo ci) {
    StrikeManager.clear((MinecraftServer) (Object) this);
  }
}
