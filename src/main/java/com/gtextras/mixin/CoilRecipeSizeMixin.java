package com.gtextras.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI", remap = false)
public class CoilRecipeSizeMixin {

    @ModifyArg(
            method = "getJEISize",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    remap = false,
                    target = "Lcom/lowdragmc/lowdraglib/utils/Size;<init>(II)V"
            ),
            index = 1
    )
    private int gtextras$growHeight(int height) {
        int extra = 42;
        return height + extra;
    }
}
