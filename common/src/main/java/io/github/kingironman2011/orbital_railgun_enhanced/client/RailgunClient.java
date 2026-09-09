package io.github.kingironman2011.orbital_railgun_enhanced.client;

import io.github.kingironman2011.orbital_railgun_enhanced.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.*;

public final class RailgunClient {
  public static final class Effect {
    public final StrikePayload payload;
    public int age;
    final SoundInstance sound;

    Effect(StrikePayload p, SoundInstance s) {
      payload = p;
      age = p.age();
      sound = s;
    }
  }

  public static final Map<Integer, Effect> EFFECTS = new LinkedHashMap<>();
  public static BlockHitResult target;
  private static boolean equipped, aimed, attackDown;
  private static int shotDelay;
  private static Identifier dimension;

  public static boolean aiming() {
    var p = Minecraft.getInstance().player;
    return p != null && p.isUsingItem() && p.getUseItem().getItem() instanceof RailgunItem;
  }

  public static void receive(StrikePayload p) {
    var mc = Minecraft.getInstance();
    if (p.stop()) {
      var old = EFFECTS.remove(p.id());
      if (old != null && old.sound != null) mc.getSoundManager().stop(old.sound);
      return;
    }
    if (mc.level == null
        || !mc.level.dimension().identifier().equals(p.dimension())
        || EFFECTS.containsKey(p.id())) return;
    var c = ClientConfig.INSTANCE;
    SoundInstance sound = null;
    if (c.enableShootSound && p.age() < StrikeMath.END_TICK) {
      sound =
          new SimpleSoundInstance(
              OrbitalRailgun.SHOOT.location(),
              SoundSource.PLAYERS,
              (float) c.shootVolume,
              1,
              RandomSource.create(),
              false,
              0,
              SoundInstance.Attenuation.NONE,
              0,
              0,
              0,
              true);
      mc.getSoundManager().play(sound);
    }
    EFFECTS.put(p.id(), new Effect(p, sound));
  }

  public static void input() {
    var mc = Minecraft.getInstance();
    boolean down = mc.options.keyAttack.isDown();
    if (aiming() && down && !attackDown && shotDelay == 0 && target != null) {
      OrbitalRailgun.platform.requestShot();
      shotDelay = 10;
    }
    attackDown = down;
  }

  public static void tick() {
    var mc = Minecraft.getInstance();
    var c = ClientConfig.INSTANCE;
    if (mc.level == null || mc.player == null) {
      clear();
      return;
    }
    var now = mc.level.dimension().identifier();
    if (!now.equals(dimension)) {
      clear();
      dimension = now;
      OrbitalRailgun.platform.requestSettings(
          new ServerSettings.Request(ServerSettings.READ, false));
    }
    if (mc.isPaused()) return;
    if (shotDelay > 0) shotDelay--;
    boolean holding =
        mc.player.getMainHandItem().getItem() instanceof RailgunItem
            || mc.player.getOffhandItem().getItem() instanceof RailgunItem;
    boolean aiming = aiming();
    if (holding && !equipped && c.enableEquipSound)
      mc.getSoundManager()
          .play(
              SimpleSoundInstance.forLocalAmbience(OrbitalRailgun.EQUIP, 1, (float) c.equipVolume));
    if (aiming && !aimed && c.enableScopeSound)
      mc.getSoundManager()
          .play(
              SimpleSoundInstance.forLocalAmbience(OrbitalRailgun.SCOPE, 1, (float) c.scopeVolume));
    equipped = holding;
    aimed = aiming;
    var hit = aiming ? Targeting.pick(mc.player, mc.options.renderDistance().get()) : null;
    target = hit != null && hit.getType() == HitResult.Type.BLOCK ? hit : null;
    for (var it = EFFECTS.values().iterator(); it.hasNext(); ) {
      var e = it.next();
      e.age++;
      if (e.age >= StrikeMath.END_TICK) {
        if (e.sound != null) mc.getSoundManager().stop(e.sound);
        it.remove();
        continue;
      }
      if (!c.enableVisualEffects
          || !(ConfigScreen.latest == null
              ? e.payload.particles()
              : ConfigScreen.latest.particles())) continue;
      var pos = Vec3.atCenterOf(e.payload.pos());
      double radius =
          (e.age < 80 ? 1.25 : Math.min(24, 2 + (e.age / 20.0 - 4) * .75))
              * e.payload.radius()
              / 24.0;
      for (int i = 0; i < 12; i++) {
        double angle = i * Math.PI / 6 + e.age * .035;
        mc.level.addParticle(
            new DustParticleOptions(0x9eedef, 1.5f),
            true,
            true,
            pos.x + Math.cos(angle) * radius,
            pos.y + .15,
            pos.z + Math.sin(angle) * radius,
            0,
            0,
            0);
      }
    }
  }

  public static void clear() {
    var mc = Minecraft.getInstance();
    for (var e : EFFECTS.values()) if (e.sound != null) mc.getSoundManager().stop(e.sound);
    EFFECTS.clear();
    target = null;
    dimension = null;
    ConfigScreen.latest = null;
    equipped = false;
    aimed = false;
    attackDown = false;
    shotDelay = 0;
  }

  public static void hud(GuiGraphicsExtractor g) {
    if (!aiming() || !ClientConfig.INSTANCE.enableVisualEffects) return;
    if (ClientConfig.INSTANCE.enableShaderEffects && !EffectsRenderer.shaderPackActive()) return;
    int x = g.guiWidth() / 2,
        y = g.guiHeight() / 2,
        color = target != null ? 0xff43ff6b : 0xffff6655;
    g.outline(x - 24, y - 24, 48, 48, color);
    g.horizontalLine(x - 8, x + 8, y, color);
    g.verticalLine(x, y - 8, y + 8, color);
    if (target != null) {
      var p = target.getBlockPos();
      g.centeredText(
          Minecraft.getInstance().font,
          p.getX() + " / " + p.getY() + " / " + p.getZ(),
          x,
          y + 32,
          color);
    }
  }
}
