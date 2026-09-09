package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import io.github.kingironman2011.orbital_railgun_enhanced.RailgunItem;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ZoomMixin {
  @Inject(method = "isScoping", at = @At("HEAD"), cancellable = true)
  private void ore$zoom(CallbackInfoReturnable<Boolean> ci) {
    var p = (Player) (Object) this;
    if (p.isUsingItem() && p.getUseItem().getItem() instanceof RailgunItem) ci.setReturnValue(true);
  }
}
