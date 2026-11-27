package com.gtextras.gt.rotation;

import com.lowdragmc.lowdraglib.gui.factory.BlockEntityUIFactory;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RotationInterfaceBlock extends RotatedPillarKineticBlock implements IBE<RotationInterfaceBlockEntity> {

    public RotationInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return true;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<RotationInterfaceBlockEntity> getBlockEntityClass() {
        return RotationInterfaceBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotationInterfaceBlockEntity> getBlockEntityType() {
        return RotationRegistry.INTERFACE_TYPE.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (AllItems.WRENCH.isIn(player.getItemInHand(hand))) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof RotationInterfaceBlockEntity be) {
            BlockEntityUIFactory.INSTANCE.openUI(be, serverPlayer);
        }
        return InteractionResult.CONSUME;
    }
}
