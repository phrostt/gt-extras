package com.gtextras.gt.rotation.coupler;

<<<<<<< HEAD
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
=======
import com.gregtechceu.gtceu.api.machine.MetaMachine;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

import com.gtextras.gt.rotation.RotationReaderMachine;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class RotationCouplerBlockEntity extends KineticBlockEntity {

<<<<<<< HEAD
    public static final float MIN_IMPACT = 1.0f;
    private static final int INTERVAL = 10;

    private boolean loaded;
    private float impact;
=======
    public static final float IMPACT = 16.0f;
    private static final int INTERVAL = 10;

    private boolean loaded;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

    public RotationCouplerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float calculateStressApplied() {
<<<<<<< HEAD
        float applied = loaded ? impact : 0.0f;
        this.lastStressApplied = applied;
        return applied;
=======
        float impact = loaded ? IMPACT : 0.0f;
        this.lastStressApplied = impact;
        return impact;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
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
<<<<<<< HEAD
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
        float newImpact = MIN_IMPACT;
        if (working) {
            GTRecipe recipe = source.getRecipeLogic().getLastRecipe();
            if (recipe != null) {
                newImpact = Math.max(MIN_IMPACT, (float) Math.abs(RecipeHelper.getRealEUt(recipe).getTotalEU()));
            }
        }
        if (working != loaded || (working && newImpact != impact)) {
            loaded = working;
            impact = newImpact;
=======
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
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
            if (hasNetwork()) {
                getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
            }
            notifyUpdate();
        }
    }
}
