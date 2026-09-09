package io.github.kingironman2011.orbital_railgun_enhanced;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class RailgunItem extends Item {
  public RailgunItem(Properties properties) {
    super(properties);
  }

  @Override
  public int getUseDuration(ItemStack stack, LivingEntity entity) {
    return 72000;
  }

  @Override
  public ItemUseAnimation getUseAnimation(ItemStack stack) {
    return ItemUseAnimation.BOW;
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand)))
      return InteractionResult.FAIL;
    player.startUsingItem(hand);
    return InteractionResult.CONSUME;
  }
}
