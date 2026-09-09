package io.github.kingironman2011.orbital_railgun_enhanced.client;

import io.github.kingironman2011.orbital_railgun_enhanced.StrikeMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Visual projection movement only; never changes player position or aiming rotation. */
public final class CameraShake {
  public static void apply(Matrix4f projection, float partialTick) {
    var mc = Minecraft.getInstance();
    var config = ClientConfig.INSTANCE;
    if (mc.player == null
        || !config.enableVisualEffects
        || !config.enableCameraShake
        || config.cameraShakeIntensity <= 0) return;
    double x = 0, y = 0, z = 0;
    for (var effect : RailgunClient.EFFECTS.values()) {
      double age = effect.age + partialTick;
      double distance =
          mc.gameRenderer
              .gameRenderState()
              .levelRenderState
              .cameraRenderState
              .pos
              .distanceTo(Vec3.atCenterOf(effect.payload.pos()));
      double strength =
          StrikeMath.shakeStrength(
              age, distance, effect.payload.radius(), config.cameraShakeIntensity);
      double t = age - StrikeMath.IMPACT_TICK, phase = effect.payload.id() * 1.73;
      x += strength * Math.sin(t * 1.7 + phase);
      y += strength * Math.sin(t * 2.3 + phase * .7);
      z += strength * Math.sin(t * 1.3 + phase * 1.3) * .5;
    }
    float radians = (float) (Math.PI / 180);
    projection.rotateXYZ(
        (float) Math.clamp(x, -2, 2) * radians,
        (float) Math.clamp(y, -2, 2) * radians,
        (float) Math.clamp(z, -1, 1) * radians);
  }
}
