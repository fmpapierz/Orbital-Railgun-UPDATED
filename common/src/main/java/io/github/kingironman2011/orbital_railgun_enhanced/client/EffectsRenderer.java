package io.github.kingironman2011.orbital_railgun_enhanced.client;

import com.mojang.blaze3d.buffers.*;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.kingironman2011.orbital_railgun_enhanced.*;
import io.github.kingironman2011.orbital_railgun_enhanced.mixin.PostChainAccessor;
import io.github.kingironman2011.orbital_railgun_enhanced.mixin.PostPassAccessor;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.gizmos.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

public final class EffectsRenderer {
  private static final Identifier AIM = OrbitalRailgun.id("aim"),
      STRIKE = OrbitalRailgun.id("strike");
  private static final int BUFFER_SIZE = 176;
  private static boolean checkedIris;
  private static Object iris;
  private static Method shaderPack;
  private static final Matrix4f inverseProjection = new Matrix4f();

  public static void setProjection(Matrix4f projection) {
    inverseProjection.set(projection).invert();
  }

  public static boolean shaderPackActive() {
    if (!checkedIris) {
      checkedIris = true;
      try {
        Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
        iris = api.getMethod("getInstance").invoke(null);
        shaderPack = api.getMethod("isShaderPackInUse");
      } catch (ReflectiveOperationException ignored) {
      }
    }
    try {
      return iris != null && (boolean) shaderPack.invoke(iris);
    } catch (ReflectiveOperationException e) {
      return true;
    }
  }

  public static void render(GraphicsResourceAllocator pool, float partialTick) {
    var mc = Minecraft.getInstance();
    var c = ClientConfig.INSTANCE;
    if (mc.level == null || !c.enableVisualEffects || !c.enableShaderEffects || shaderPackActive())
      return;
    if (RailgunClient.aiming())
      apply(
          AIM,
          RailgunClient.target == null
              ? Vec3.ZERO
              : Vec3.atCenterOf(RailgunClient.target.getBlockPos()),
          (mc.player.getTicksUsingItem() + partialTick) / 20f,
          RailgunClient.target != null,
          ConfigScreen.latest == null ? 24 : ConfigScreen.latest.strikeRadius(),
          1,
          pool);
    for (var effect : RailgunClient.EFFECTS.values())
      apply(
          STRIKE,
          Vec3.atCenterOf(effect.payload.pos()),
          (effect.age + partialTick) / 20f,
          true,
          effect.payload.radius(),
          StrikeMath.effectStrength(effect.age + partialTick),
          pool);
  }

  private static void apply(
      Identifier id,
      Vec3 target,
      float time,
      boolean hit,
      int radius,
      float strength,
      GraphicsResourceAllocator pool) {
    var mc = Minecraft.getInstance();
    var renderer = mc.gameRenderer;
    PostChain chain = mc.getShaderManager().getPostChain(id, LevelTargetBundle.MAIN_TARGETS);
    if (chain == null) return;
    var camera = renderer.gameRenderState().levelRenderState.cameraRenderState;
    for (PostPass pass : ((PostChainAccessor) chain).ore$passes()) {
      var uniforms = ((PostPassAccessor) pass).ore$uniforms();
      if (!uniforms.containsKey("OreData")) continue;
      var buffer = uniforms.get("OreData");
      if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
        buffer.close();
        buffer =
            RenderSystem.getDevice()
                .createBuffer(
                    () -> "Orbital railgun uniforms",
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    BUFFER_SIZE);
        uniforms.put("OreData", buffer);
      }
      try (var stack = MemoryStack.stackPush()) {
        var builder = Std140Builder.onStack(stack, BUFFER_SIZE);
        builder.putMat4f(inverseProjection);
        builder.putMat4f(camera.viewRotationMatrix);
        builder.putVec4(
            (float) camera.pos.x, (float) camera.pos.y, (float) camera.pos.z, radius / 24f);
        builder.putVec4((float) target.x, (float) target.y, (float) target.z, hit ? 1 : 0);
        builder.putVec4(
            time, renderer.mainRenderTarget().width, renderer.mainRenderTarget().height, strength);
        RenderSystem.getDevice()
            .createCommandEncoder()
            .writeToBuffer(buffer.slice(), builder.get());
      }
    }
    chain.process(renderer.mainRenderTarget(), pool);
  }

  public static void geometry() {
    var mc = Minecraft.getInstance();
    var c = ClientConfig.INSTANCE;
    if (mc.level == null || !c.enableVisualEffects || c.enableShaderEffects && !shaderPackActive())
      return;
    try (var collector = mc.levelExtractor.collectPerFrameMainThreadGizmos()) {
      if (RailgunClient.target != null)
        Gizmos.cuboid(RailgunClient.target.getBlockPos(), 0.004f, GizmoStyle.stroke(0xff43ff6b, 2));
      for (var effect : RailgunClient.EFFECTS.values()) {
        var center = Vec3.atCenterOf(effect.payload.pos());
        float radius =
            (effect.age < 80 ? 1.25f : Math.min(24, 2 + (effect.age / 20f - 4) * .75f))
                * effect.payload.radius()
                / 24f;
        int color = (Math.round(255 * StrikeMath.effectStrength(effect.age)) << 24) | 0x9eeded;
        Gizmos.circle(center, radius, GizmoStyle.stroke(color, 3));
        Gizmos.line(center, center.add(0, 256, 0), color, 4);
        Gizmos.cuboid(effect.payload.pos(), GizmoStyle.stroke(color, 2));
      }
    }
  }
}
