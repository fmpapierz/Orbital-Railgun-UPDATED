package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.OrbitalRailgun;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class SmokeServerMixin {
  @Unique private boolean ore$finished;

  @Inject(method = "tickServer", at = @At("TAIL"))
  private void ore$server(CallbackInfo ci) {
    var server = (MinecraftServer) (Object) this;
    if (!server.isDedicatedServer() || ore$finished || server.getTickCount() < 40) return;
    if (server.overworld() == null
        || BuiltInRegistries.ITEM.getValue(OrbitalRailgun.ITEM_KEY) == null
        || server.getCommands().getDispatcher().getRoot().getChild("ore") == null)
      throw new IllegalStateException("Dedicated smoke failure");
    ore$finished = true;
    OrbitalRailgun.LOGGER.info(
        "ORE_SMOKE_DEDICATED_PASS loader={}", OrbitalRailgun.platform.loader());
    server.halt(false);
  }
}
