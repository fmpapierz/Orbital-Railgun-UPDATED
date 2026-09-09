package io.github.kingironman2011.orbital_railgun_enhanced.mixin;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import io.github.kingironman2011.orbital_railgun_enhanced.client.EffectsRenderer;
import net.minecraft.client.DeltaTracker;
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
                  "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"),
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

  @Inject(
      method = "renderLevel",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V"))
  private void ore$effects(DeltaTracker deltaTracker, CallbackInfo ci) {
    // The world depth is discarded here for the hand. Post processing must precede it.
    EffectsRenderer.render(resourcePool, deltaTracker.getGameTimeDeltaPartialTick(false));
  }
}
