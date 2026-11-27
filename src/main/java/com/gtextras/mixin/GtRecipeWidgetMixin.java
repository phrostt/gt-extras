package com.gtextras.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget", remap = false)
public class GtRecipeWidgetMixin {

    @Inject(method = "setRecipeWidget", at = @At("TAIL"), remap = false, require = 0)
    private void gtextras$recordLines(CallbackInfo ci) {
        com.gtextras.NativeLines.record(this);
    }

    @Inject(method = "initializeRecipeTextWidget", at = @At("TAIL"), remap = false, require = 0)
    private void gtextras$placeRow(CallbackInfo ci) {
        com.gtextras.RecipeRow.apply(this);
    }

    @Inject(method = "setRecipeOC", at = @At("TAIL"), remap = false, require = 0)
    private void gtextras$replaceVoltage(int tier, boolean flag, CallbackInfo ci) {
        com.gtextras.RecipeRow.apply(this);
    }

    @ModifyArg(
            method = "collectStorage",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/Table;put(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    ordinal = 0
            ),
            index = 2,
            remap = false,
            require = 0
    )
    private Object gtextras$moveCircuitToBadge(Object contents) {
        return com.gtextras.CircuitBadge.strip(contents);
    }
}
