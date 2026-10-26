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
<<<<<<< HEAD
        int extra = com.gtextras.CoilInfo.JEI_EXTRA_HEIGHT;
=======
        int extra = 20;
        try {
            if (net.minecraftforge.fml.loading.LoadingModList.get().getModFileById("emi") == null) {
                extra = 42;
            }
        } catch (Throwable t) {
            extra = 20;
        }
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
        return height + extra;
    }
}
