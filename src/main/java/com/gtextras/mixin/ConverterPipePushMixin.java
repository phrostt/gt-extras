package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import de.maxhenkel.pipez.blocks.EnergyPipeBlock;
import de.maxhenkel.pipez.blocks.PipeBlock;
import de.maxhenkel.pipez.blocks.UniversalPipeBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A GregTech converter in EU to FE mode normally pushes Forge Energy into the block in front
 * of it. Pipez pipes that carry no settings have no block entity and so cannot take a push at
 * all. When the block in front is a Pipez energy pipe, this walks the connected energy pipes
 * itself and hands the converter's output to every block on the network that accepts energy,
 * with no per-face transfer limit. A face set to Extract is left alone so the pipe can pull.
 */
@Mixin(value = ConverterTrait.class, remap = false)
public abstract class ConverterPipePushMixin {

    private static final int MAX_PIPES = 8192;
    private static final int REFRESH_TICKS = 40;

    @Unique
    private long gtxTicks;
    @Unique
    private long gtxNextScan;
    @Unique
    private int gtxStart;
    @Unique
    private List<BlockPos> gtxEndPos;
    @Unique
    private List<Direction> gtxEndFace;

    @Unique
    private static boolean gtxCarriesEnergy(Block block) {
        return block instanceof EnergyPipeBlock || block instanceof UniversalPipeBlock;
    }

    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void gtextras$pushIntoPipes(CallbackInfo ci) {
        ConverterTrait trait = (ConverterTrait) (Object) this;
        if (trait.isFeToEu()) {
            return;
        }
        MetaMachine machine = ((MachineTrait) (Object) this).getMachine();
        Level level = machine.getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        Direction front = machine.getFrontFacing();
        BlockPos frontPos = machine.getPos().relative(front);
        BlockState frontState = level.getBlockState(frontPos);
        if (!(frontState.getBlock() instanceof PipeBlock frontBlock) || !gtxCarriesEnergy(frontBlock)) {
            return;
        }
        Direction toConverter = front.getOpposite();
        if (!frontState.getValue(frontBlock.getProperty(toConverter)) || frontBlock.isExtracting(level, frontPos, toConverter)) {
            return;
        }
        ci.cancel();

        if (gtxEndPos == null) {
            gtxEndPos = new ArrayList<>();
            gtxEndFace = new ArrayList<>();
        }
        long now = gtxTicks++;
        if (now >= gtxNextScan) {
            gtxScan(level, machine.getPos(), frontPos);
            gtxNextScan = now + REFRESH_TICKS;
        }
        if (gtxEndPos.isEmpty()) {
            return;
        }

        long stored = trait.getEnergyStored();
        long budgetEu = Math.min(stored, trait.getVoltage() * (long) trait.getAmps());
        if (budgetEu <= 0) {
            return;
        }
        int ratio = FeCompat.ratio(false);
        long budgetFe = FeCompat.toFeLong(budgetEu, ratio);
        long remaining = budgetFe;
        int n = gtxEndPos.size();
        int start = Math.floorMod(gtxStart++, n);
        for (int i = 0; i < n && remaining > 0; i++) {
            int idx = (start + i) % n;
            BlockEntity be = level.getBlockEntity(gtxEndPos.get(idx));
            if (be == null) {
                continue;
            }
            IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, gtxEndFace.get(idx)).orElse(null);
            if (storage == null || !storage.canReceive()) {
                continue;
            }
            int offer = (int) Math.min(remaining, (long) Integer.MAX_VALUE);
            int taken = storage.receiveEnergy(offer, false);
            if (taken > 0) {
                remaining -= taken;
            }
        }
        long spentFe = budgetFe - remaining;
        if (spentFe > 0) {
            long spentEu = (spentFe + ratio - 1) / ratio;
            trait.setEnergyStored(Math.max(0L, stored - spentEu));
        }
    }

    @Unique
    private void gtxScan(Level level, BlockPos converterPos, BlockPos startPipe) {
        gtxEndPos.clear();
        gtxEndFace.clear();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(startPipe);
        seen.add(startPipe);
        while (!queue.isEmpty() && seen.size() <= MAX_PIPES) {
            BlockPos pos = queue.poll();
            if (!level.isLoaded(pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof PipeBlock pipe) || !gtxCarriesEnergy(pipe)) {
                continue;
            }
            for (Direction dir : Direction.values()) {
                if (!state.getValue(pipe.getProperty(dir))) {
                    continue;
                }
                BlockPos next = pos.relative(dir);
                if (next.equals(converterPos) || !level.isLoaded(next)) {
                    continue;
                }
                if (level.getBlockState(next).getBlock() instanceof PipeBlock other) {
                    if (gtxCarriesEnergy(other) && seen.add(next)) {
                        queue.add(next);
                    }
                    continue;
                }
                if (pipe.isExtracting(level, pos, dir)) {
                    continue;
                }
                BlockEntity be = level.getBlockEntity(next);
                if (be == null) {
                    continue;
                }
                IEnergyStorage storage = be.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).orElse(null);
                if (storage != null && storage.canReceive()) {
                    gtxEndPos.add(next);
                    gtxEndFace.add(dir.getOpposite());
                }
            }
        }
    }
}
