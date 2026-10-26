package com.gtextras.gt.rotation.coupler;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;

import com.gtextras.gt.rotation.RotationReaderMachine;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RotationMotorBlockEntity extends GeneratingKineticBlockEntity {

    public static final float SPEED = 64.0f;
    public static final float SU_PER_EU = 64.0f;
    public static final long MIN_EUT = 1L;
    private static final int INTERVAL = 10;

    private boolean running;
    private long activeEut;

    public RotationMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getGeneratedSpeed() {
        return running ? SPEED : 0.0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = running ? Math.max(activeEut, MIN_EUT) * SU_PER_EU / SPEED : 0.0f;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public void tick() {
        super.tick();
        Level level = getLevel();
        if (level == null || level.isClientSide || level.getGameTime() % INTERVAL != 0) {
            return;
        }
        IRecipeLogicMachine source = null;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(getBlockPos().relative(dir));
            if (be instanceof IMachineBlockEntity holder) {
                MetaMachine machine = holder.getMetaMachine();
                if (machine instanceof RotationReaderMachine reader) {
                    source = reader.workingController();
                } else if (machine instanceof IRecipeLogicMachine recipeMachine
                        && recipeMachine.getRecipeLogic().isWorking()) {
                    source = recipeMachine;
                }
                if (source != null) break;
            }
        }
        boolean working = source != null;
        long eut = 0L;
        if (working) {
            GTRecipe recipe = source.getRecipeLogic().getLastRecipe();
            if (recipe != null) {
                eut = Math.max(0L, RecipeHelper.getRealEUt(recipe).getTotalEU());
            }
        }
        if (working != running || (working && eut != activeEut)) {
            running = working;
            activeEut = eut;
            updateGeneratedRotation();
        }
    }
}
