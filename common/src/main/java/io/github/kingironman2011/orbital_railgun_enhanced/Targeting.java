package io.github.kingironman2011.orbital_railgun_enhanced;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.BlockHitResult;

/** Trace visible terrain without generating chunks beyond the loaded world. */
public final class Targeting {
  public static double horizontalReach(int chunks) {
    return (Math.clamp(chunks, 2, 128) + 1) * 16.0;
  }

  public static double rayLength(int chunks, double verticalReach) {
    return Math.hypot(horizontalReach(chunks) * Math.sqrt(2), verticalReach);
  }

  public static BlockHitResult pick(Entity player, int chunks) {
    var level = player.level();
    var eye = player.getEyePosition(1);
    double reach = horizontalReach(chunks);
    double vertical =
        Math.max(Math.abs(eye.y - level.getMinY()), Math.abs(eye.y - level.getMaxY())) + 1;
    var end = eye.add(player.getViewVector(1).scale(rayLength(chunks, vertical)));
    BlockGetter loaded =
        new BlockGetter() {
          private boolean available(BlockPos p) {
            return Math.abs(p.getX() - eye.x) <= reach
                && Math.abs(p.getZ() - eye.z) <= reach
                && !level.isOutsideBuildHeight(p)
                && level.hasChunkAt(p);
          }

          public BlockState getBlockState(BlockPos p) {
            return available(p) ? level.getBlockState(p) : Blocks.AIR.defaultBlockState();
          }

          public FluidState getFluidState(BlockPos p) {
            return available(p) ? level.getFluidState(p) : Fluids.EMPTY.defaultFluidState();
          }

          public BlockEntity getBlockEntity(BlockPos p) {
            return available(p) ? level.getBlockEntity(p) : null;
          }

          public int getHeight() {
            return level.getHeight();
          }

          public int getMinY() {
            return level.getMinY();
          }
        };
    return loaded.clip(
        new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
  }
}
