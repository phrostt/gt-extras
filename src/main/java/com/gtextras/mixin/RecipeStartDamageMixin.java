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
public abstract class RecipeStartDamageMixin {

    @Inject(method = "setupRecipe",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;setStatus(Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic$Status;)V",
                    ordinal = 1),
            remap = false)
    private void gtextras$damageItems(GTRecipe recipe, CallbackInfo ci) {
        RecipeLogic self = (RecipeLogic) (Object) this;
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof DamageItemCondition damage) {
                damage.consume(self, recipe);
            }
        }
    }

    @Inject(method = "onRecipeFinish", at = @At("HEAD"), remap = false)
    private void gtextras$clearOnFinish(CallbackInfo ci) {
        DamageItemCondition.clearPaid((RecipeLogic) (Object) this);
    }

    @Inject(method = "setStatus", at = @At("HEAD"), remap = false)
    private void gtextras$clearOnStatus(RecipeLogic.Status status, CallbackInfo ci) {
        if (status != RecipeLogic.Status.WORKING) {
            DamageItemCondition.clearPaid((RecipeLogic) (Object) this);
        }
    }
}
