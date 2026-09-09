package io.github.kingironman2011.orbital_railgun_enhanced;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StrikeMathTest {
  @Test
  void rangeCoversRenderedCornersAndVerticalTerrain() {
    assertTrue(Targeting.horizontalReach(32) > 512);
    assertTrue(Targeting.rayLength(32, 384) > Math.sqrt(512 * 512 * 2 + 384 * 384));
    assertTrue(Targeting.rayLength(32, 384) > Targeting.rayLength(6, 384));
  }

  @Test
  void impactShakeStartsAtImpactAndDecaysWithDistanceAndIntensity() {
    assertEquals(0, StrikeMath.shakeStrength(699, 0, 24, 1));
    assertEquals(0, StrikeMath.shakeStrength(700, 0, 24, 1));
    assertEquals(0, StrikeMath.shakeStrength(760, 0, 24, 1));
    assertEquals(0, StrikeMath.shakeStrength(710, 0, 24, 0));
    double strong = StrikeMath.shakeStrength(710, 0, 24, 1);
    assertTrue(strong > 0);
    assertEquals(strong * .5, StrikeMath.shakeStrength(710, 0, 24, .5), 1e-9);
    assertTrue(StrikeMath.shakeStrength(710, 200, 24, 1) < strong);
    assertTrue(StrikeMath.shakeStrength(750, 0, 24, 1) < strong);
  }

  @Test
  void fadeIsSmoothMonotonicAndCompleteBeforeRemoval() {
    assertEquals(1f, StrikeMath.effectStrength(960));
    assertEquals(.5f, StrikeMath.effectStrength(1010));
    assertEquals(0f, StrikeMath.effectStrength(1060));
    float previous = 1;
    for (float age = 950; age <= 1070; age += .25f) {
      float current = StrikeMath.effectStrength(age);
      assertTrue(current >= 0 && current <= previous);
      assertTrue(previous - current < .004f);
      previous = current;
    }
    assertTrue(StrikeMath.inColumn(12, 0, 12));
    assertFalse(StrikeMath.inColumn(13, 0, 12));
  }

  @Test
  void columnIncludesBoundaryButExcludesCorners() {
    assertTrue(StrikeMath.inColumn(24, 0));
    assertTrue(StrikeMath.inColumn(0, -24));
    assertFalse(StrikeMath.inColumn(24, 1));
    assertFalse(StrikeMath.inColumn(24, 24));
    assertFalse(StrikeMath.inColumn(25, 0));
  }

  @Test
  void pullIsFiniteAtTheTwentyBlockSingularity() {
    for (int age : new int[] {400, 401, 550, 699})
      for (double distance : new double[] {0, 19.999999, 20, 20.000001, 500}) {
        double force = StrikeMath.pull(distance, age);
        assertTrue(Double.isFinite(force));
        assertTrue(force >= 0 && force <= 5);
      }
    assertEquals(0, StrikeMath.pull(20, 400));
    assertEquals(5, StrikeMath.pull(20, 699));
  }

  @Test
  void pullDoesNotBeginBeforeTheChargePhase() {
    assertEquals(0, StrikeMath.pull(10, 399));
  }
}
