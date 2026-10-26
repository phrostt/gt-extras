package com.gtextras.gt.rotation.coupler;

import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.gt.rotation.RotationReaderMachine;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RotationCouplerBlockEntity extends KineticBlockEntity {

    public static final float IMPACT = 16.0f;
    private static final int INTERVAL = 10;

    private boolean loaded;

    public RotationCouplerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        float impact = loaded ? IMPACT : 0.0f;
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    public float calculateAddedStressCapacity() {
        this.lastCapacityProvided = 0.0f;
        return 0.0f;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();
        if (level == null || level.isClientSide || level.getGameTime() % INTERVAL != 0) {
            return;
        }
        boolean working = false;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(getBlockPos().relative(dir));
            if (be instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity holder) {
                MetaMachine machine = holder.getMetaMachine();
                if (machine instanceof RotationReaderMachine reader && reader.isControllerWorking()) {
                    working = true;
                    break;
                }
                if (machine instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine recipeMachine
                        && recipeMachine.getRecipeLogic().isWorking()) {
                    working = true;
                    break;
                }
            }
        }
        if (working != loaded) {
            loaded = working;
            if (hasNetwork()) {
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            }
            notifyUpdate();
        }
    }
}
