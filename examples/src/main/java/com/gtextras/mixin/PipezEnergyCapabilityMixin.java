package com.gtextras.mixin;

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import de.maxhenkel.pipez.utils.PipeEnergyStorage;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gtextras.GTExtras;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pipez hides an energy pipe's Forge Energy capability on every face that is not set to
 * Extract, so nothing can find the pipe to push into. This hands out the capability only on a
 * face that touches the output side of a GregTech converter running EU to FE; every other
 * face and neighbour is left exactly as Pipez makes it. The storage given out is never
 * ticked, so such a face still never pulls anything.
 */
@Mixin(value = PipeLogicTileEntity.class, remap = false)
public abstract class PipezEnergyCapabilityMixin {

    private static long gtxAsks = 0;
    private static long gtxAlive = 0;

    @Inject(
            method = "getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private <T> void gtextras$exposeEnergyOnAnyFace(Capability<T> cap, Direction side, CallbackInfoReturnable<LazyOptional<T>> cir) {
        if (cap == ForgeCapabilities.ENERGY && side != null && gtxAlive++ % 2000 == 0) {
            GTExtras.LOGGER.info("[gtextras] pipe capability hook alive: pipe asked for energy on face {}, already present={}",
                    side, cir.getReturnValue().isPresent());
        }
        if (cap != ForgeCapabilities.ENERGY || side == null || cir.getReturnValue().isPresent()) {
            return;
        }
        PipeLogicTileEntity pipe = (PipeLogicTileEntity) (Object) this;
        if (pipe.isRemoved() || !pipe.hasType(EnergyPipeType.INSTANCE) || pipe.getLevel() == null) {
            return;
        }
        BlockEntity neighbour = pipe.getLevel().getBlockEntity(pipe.getBlockPos().relative(side));
        if (neighbour instanceof MetaMachineBlockEntity asked && gtxAsks++ % 200 == 0) {
            if (asked.getMetaMachine() instanceof ConverterMachine c) {
                GTExtras.LOGGER.info("[gtextras] pipe at {} face {}: converter at {} front={} wantedFront={} feToEu={}",
                        pipe.getBlockPos(), side, neighbour.getBlockPos(), c.getFrontFacing(), side.getOpposite(), c.getConverterTrait().isFeToEu());
            } else {
                GTExtras.LOGGER.info("[gtextras] pipe at {} face {}: GT machine {} is not a converter ({})",
                        pipe.getBlockPos(), side, neighbour.getBlockPos(), asked.getMetaMachine().getClass().getName());
            }
        }
        if (!(neighbour instanceof MetaMachineBlockEntity machineBE)
                || !(machineBE.getMetaMachine() instanceof ConverterMachine converter)
                || converter.getFrontFacing() != side.getOpposite()
                || converter.getConverterTrait().isFeToEu()) {
            return;
        }
        GTExtras.LOGGER.info("[gtextras] handing energy capability to converter at {} on pipe at {} face {}",
                neighbour.getBlockPos(), pipe.getBlockPos(), side);
        cir.setReturnValue(LazyOptional.of(() -> new PipeEnergyStorage(pipe, side)).cast());
    }
}
