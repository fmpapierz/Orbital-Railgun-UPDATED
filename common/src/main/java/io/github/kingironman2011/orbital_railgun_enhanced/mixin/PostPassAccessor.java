package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import java.util.Map;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PostPass.class)
public interface PostPassAccessor {
  @Accessor("customUniforms")
  Map<String, GpuBuffer> ore$uniforms();
}
