package io.github.kingironman2011.orbital_railgun_enhanced;

public final class StrikeMath {
  public static final int RADIUS = 24, PULL_TICK = 400, IMPACT_TICK = 700, END_TICK = 1060;

  public static boolean inColumn(int x, int z) {
    return inColumn(x, z, RADIUS);
  }

  public static boolean inColumn(int x, int z, int radius) {
    return x * x + z * z <= radius * radius;
  }

  /** Fade over the final five seconds; arrive at zero with zero slope. */
  public static float effectStrength(float age) {
    float t = Math.clamp((age - (END_TICK - 100)) / 100f, 0, 1);
    return 1 - t * t * (3 - 2 * t);
  }

  public static double pull(double distance, int age) {
    return Math.min(
        4.0 / Math.max(0.001, Math.abs(distance - 20.0)) * Math.max(0, age - PULL_TICK) / 300.0,
        5.0);
  }

  public static double shakeStrength(double age, double distance, int radius, double intensity) {
    double elapsed = age - IMPACT_TICK;
    if (elapsed <= 0 || elapsed >= 60 || !Double.isFinite(intensity)) return 0;
    double attack = Math.min(1, elapsed / 3);
    double decay = 1 - elapsed / 60;
    double attenuation = distance / Math.max(16, radius * 4.0);
    return 2
        * Math.clamp(intensity, 0, 1)
        * attack
        * decay
        * decay
        / (1 + attenuation * attenuation);
  }
}
