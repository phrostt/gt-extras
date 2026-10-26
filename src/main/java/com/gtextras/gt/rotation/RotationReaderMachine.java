package com.gtextras.gt.rotation;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class RotationReaderMachine extends TieredPartMachine implements IInteractedMachine {

    private static final int INTERVAL = 10;

    private TickableSubscription subscription;
    private volatile float rpm;

    public RotationReaderMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    public com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine workingController() {
        for (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController controller : getControllers()) {
            if (controller instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine machine
                    && machine.getRecipeLogic().isWorking()) {
                return machine;
            }
        }
        return null;
    }

    public boolean isControllerWorking() {
        for (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController controller : getControllers()) {
            if (controller instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine machine
                    && machine.getRecipeLogic().isWorking()) {
                return true;
            }
        }
        return false;
    }

    public float getRpm() {
        return rpm;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            subscription = subscribeServerTick(this::scan);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    private static Class<?> kineticClass;
    private static java.lang.reflect.Method speedMethod;
    private static java.lang.reflect.Method stressMethod;
    private static boolean lookedUp;

    private static float readSpeed(BlockEntity be) {
        if (be == null) return 0;
        try {
            if (!lookedUp) {
                lookedUp = true;
                kineticClass = Class.forName("com.simibubi.create.content.kinetics.base.KineticBlockEntity");
                speedMethod = kineticClass.getMethod("getSpeed");
                stressMethod = kineticClass.getMethod("isOverStressed");
            }
            if (kineticClass == null || !kineticClass.isInstance(be)) return 0;
            if (Boolean.TRUE.equals(stressMethod.invoke(be))) return 0;
            return Math.abs(((Number) speedMethod.invoke(be)).floatValue());
        } catch (Throwable t) {
            return 0;
        }
    }

    private void scan() {
        if (getOffsetTimer() % INTERVAL != 0) return;
        Level level = getLevel();
        if (level == null) return;
        BlockPos pos = getPos();
        float best = 0;
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            float speed = readSpeed(be);
            if (speed > best) {
                best = speed;
            }
        }
        rpm = best;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("gtextras.hatch.rotation_status", String.format("%.0f", rpm)), true);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
