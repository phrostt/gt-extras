package com.gtextras.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI", remap = false)
public class CoilRecipeSizeMixin {

    @Inject(method = "getJEISize", at = @At("RETURN"), remap = false, cancellable = true)
    private void gtextras$growHeight(CallbackInfoReturnable cir) {
        Object grown = com.gtextras.CardSize.grow(this, cir.getReturnValue());
        if (grown != null) {
            cir.setReturnValue(grown);
        }
    }
}
