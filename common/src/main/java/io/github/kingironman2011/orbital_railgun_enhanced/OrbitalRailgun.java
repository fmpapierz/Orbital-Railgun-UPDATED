package io.github.kingironman2011.orbital_railgun_enhanced;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OrbitalRailgun {
  public static final String MOD_ID = "orbital_railgun_enhanced";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static final ResourceKey<Item> ITEM_KEY =
      ResourceKey.create(Registries.ITEM, id("orbital_railgun"));
  public static final SoundEvent SHOOT = SoundEvent.createVariableRangeEvent(id("railgun_shoot"));
  public static final SoundEvent SCOPE = SoundEvent.createVariableRangeEvent(id("scope_on"));
  public static final SoundEvent EQUIP = SoundEvent.createVariableRangeEvent(id("equip"));
  public static Platform platform;

  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }

  public static Item createItem() {
    return new RailgunItem(new Item.Properties().setId(ITEM_KEY).stacksTo(1).rarity(Rarity.EPIC));
  }

  public static void init(Platform bridge) {
    platform = bridge;
    io.github.kingironman2011.orbital_railgun_enhanced.config.ServerConfig.INSTANCE.loadConfig();
    LOGGER.info("Orbital Railgun Enhanced 26.3 initialized on {}", bridge.loader());
  }
}
