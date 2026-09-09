package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import java.util.List;
import net.minecraft.client.renderer.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostChain.class)
public interface PostChainAccessor {
  @Accessor("passes")
  List<PostPass> ore$passes();
}
