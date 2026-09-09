package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.commands.Commands.class)
public abstract class CommandsMixin {
  @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

  @Inject(method = "<init>", at = @At("RETURN"))
  private void ore$commands(CallbackInfo ci) {
    io.github.kingironman2011.orbital_railgun_enhanced.Commands.register(dispatcher);
  }
}
