package com.gtextras.gt.trait;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface Linkable {

    LinkSet links();

    boolean canLink(Level level, BlockPos target);

    int linkRange();

    int[] linkColor();

    boolean linkable();

    BlockPos linkOrigin();

    void markLinksDirty();
}
