package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.kingironman2011.orbital_railgun_enhanced.client.EffectsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
  @Shadow @Final private CrossFrameResourcePool resourcePool;

  // Capture the actual terrain projection, including view bobbing and portal distortion.
  @ModifyArg(
      method = "renderLevel",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;"),
      index = 0)
  private Matrix4f ore$projection(Matrix4f projection) {
    io.github.kingironman2011.orbital_railgun_enhanced.client.CameraShake.apply(
        projection,
        net.minecraft.client.Minecraft.getInstance()
            .getDeltaTracker()
            .getGameTimeDeltaPartialTick(false));
    EffectsRenderer.setProjection(projection);
    return projection;
  }

  @Inject(method = "render3dHud", at = @At("HEAD"))
  private void ore$effects(CallbackInfo ci) {
    // 26.3 renders the hand in render3dHud. Process terrain before its depth is cleared.
    EffectsRenderer.render(
        resourcePool, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
  }
}
