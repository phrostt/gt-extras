package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.capability.compat.FeCompat;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.common.machine.electric.ConverterMachine;
import com.gregtechceu.gtceu.common.machine.trait.ConverterTrait;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * GregTech's converter exposes a Forge Energy side that never allows anything to be taken
 * out of it. While the converter runs in EU to FE mode that side is made drainable, paid for
 * from the converter's EU buffer. In the other mode nothing changes.
 */
@Mixin(targets = "com.gregtechceu.gtceu.common.machine.trait.ConverterTrait$FEContainer", remap = false)
public abstract class ConverterOutputSideMixin {

    private ConverterTrait gtextras$owner() {
        if (((MachineTrait) (Object) this).getMachine() instanceof ConverterMachine converter) {
            return converter.getConverterTrait();
        }
        return null;
    }

    @Inject(method = "canExtract", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gtextras$drainable(CallbackInfoReturnable<Boolean> cir) {
        ConverterTrait owner = gtextras$owner();
        cir.setReturnValue(owner != null && !owner.isFeToEu());
    }

    @Inject(method = "extractEnergy", at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void gtextras$drain(int requested, boolean simulate, CallbackInfoReturnable<Integer> cir) {
        ConverterTrait owner = gtextras$owner();
        if (owner == null || owner.isFeToEu() || requested <= 0) {
            return;
        }
        int perEu = FeCompat.ratio(false);
        long buffered = FeCompat.toFeLong(owner.getEnergyStored(), perEu);
        long grant = Math.min((long) requested, buffered);
        grant -= grant % perEu;
        if (grant > 0 && !simulate) {
            owner.setEnergyStored(owner.getEnergyStored() - FeCompat.toEu(grant, perEu));
        }
        cir.setReturnValue((int) grant);
    }
}
