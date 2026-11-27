package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gregtechceu.gtceu.api.recipe.ui.GTRecipeTypeUI", remap = false)
public class GtRecipeTypeUiMixin {

    @Inject(method = "appendJEIUI", at = @At("HEAD"), remap = false, require = 0)
    private void gtextras$beforeNative(GTRecipe recipe, WidgetGroup group, CallbackInfo ci) {
        com.gtextras.RecipeRow.beginNative(group);
    }

    @Inject(method = "appendJEIUI", at = @At("TAIL"), remap = false, require = 0)
    private void gtextras$afterNative(GTRecipe recipe, WidgetGroup group, CallbackInfo ci) {
        com.gtextras.RecipeRow.relocateNative(group, recipe);
    }
}
