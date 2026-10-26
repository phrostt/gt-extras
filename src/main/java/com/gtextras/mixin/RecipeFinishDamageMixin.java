package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import com.gtextras.gt.durability.DamageItemCondition;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class RecipeFinishDamageMixin {

    @Inject(method = "onRecipeFinish", at = @At("HEAD"), remap = false)
    private void gtextras$damageItems(CallbackInfo ci) {
        RecipeLogic self = (RecipeLogic) (Object) this;
        GTRecipe recipe = self.getLastRecipe();
        if (recipe == null) return;
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof DamageItemCondition damage) {
                damage.consume(self);
            }
        }
    }
}
