package com.gtextras.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget", remap = false)
public class GtRecipeWidgetMixin {

    @Inject(method = "setRecipeWidget", at = @At("TAIL"), remap = false, require = 0)
    private void gtextras$recordLines(CallbackInfo ci) {
        com.gtextras.NativeLines.record(this);
    }
}
