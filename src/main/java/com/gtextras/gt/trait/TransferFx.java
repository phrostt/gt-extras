package com.gtextras.gt.trait;

import com.hollingsworth.arsnouveau.common.entity.EntityFollowProjectile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class TransferFx {

    public static final int[] MANA = { 70, 170, 255 };
    public static final int[] SOURCE = { 255, 40, 190 };

    private TransferFx() {}

    public static void orb(Level level, BlockPos from, BlockPos to, int[] color) {
        if (level == null || level.isClientSide || !HatchConfig.particles()) return;
        if (from.equals(to)) return;
        level.addFreshEntity(new EntityFollowProjectile(level, from, to, color[0], color[1], color[2]));
    }
}
