package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import com.gtextras.GTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ConverterTrait.class, remap = false)
public abstract class ConverterTickProbeMixin {

    private long gtxProbeTicks = 0;

    @Inject(method = "serverTick", at = @At("HEAD"), remap = false)
    private void gtextras$probe(CallbackInfo ci) {
        if (gtxProbeTicks++ % 400 != 0) {
            return;
        }
        ConverterTrait trait = (ConverterTrait) (Object) this;
        MetaMachine machine = ((MachineTrait) (Object) this).getMachine();
        Level level = machine.getLevel();
        if (level == null) {
            return;
        }
        Direction front = machine.getFrontFacing();
        BlockPos target = machine.getPos().relative(front);
        BlockEntity be = level.getBlockEntity(target);
        IEnergyStorage storage = trait.isFeToEu() ? null : GTCapabilityHelper.getForgeEnergy(level, target, front.getOpposite());
        GTExtras.LOGGER.info("[gtextras] converter at {} ticking: feToEu={} front={} frontBlock={} frontBE={} energyCap={} canReceive={} storedEU={}",
                machine.getPos(), trait.isFeToEu(), front, level.getBlockState(target).getBlock(),
                be == null ? "none" : be.getClass().getSimpleName(),
                storage == null ? "none" : storage.getClass().getSimpleName(),
                storage != null && storage.canReceive(), trait.getEnergyStored());
    }
}
