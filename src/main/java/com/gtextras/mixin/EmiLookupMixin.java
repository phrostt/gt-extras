package com.gtextras.mixin;

import com.gtextras.EmiLookup;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ModularEmiRecipe.class, remap = false)
public abstract class EmiLookupMixin {

    @Inject(method = "getInputs", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void gtextras$inputs(CallbackInfoReturnable<List<EmiIngredient>> cir) {
        List<EmiIngredient> extra = EmiLookup.inputs((EmiRecipe) (Object) this);
        if (!extra.isEmpty()) {
            List<EmiIngredient> merged = new java.util.ArrayList<>(cir.getReturnValue());
            merged.addAll(extra);
            cir.setReturnValue(merged);
        }
    }

    @Inject(method = "getOutputs", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private void gtextras$outputs(CallbackInfoReturnable<List<EmiStack>> cir) {
        List<EmiStack> extra = EmiLookup.outputs((EmiRecipe) (Object) this);
        if (!extra.isEmpty()) {
            List<EmiStack> merged = new java.util.ArrayList<>(cir.getReturnValue());
            merged.addAll(extra);
            cir.setReturnValue(merged);
        }
    }
}
