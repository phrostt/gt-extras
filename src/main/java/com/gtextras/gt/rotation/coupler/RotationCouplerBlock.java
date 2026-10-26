package com.gtextras.gt.rotation.coupler;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RotationCouplerBlock extends RotatedPillarKineticBlock implements IBE<RotationCouplerBlockEntity> {

    public RotationCouplerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<RotationCouplerBlockEntity> getBlockEntityClass() {
        return RotationCouplerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotationCouplerBlockEntity> getBlockEntityType() {
        return CouplerRegistry.COUPLER_TYPE.get();
    }
}
