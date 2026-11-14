package com.gtextras.mixin;

import com.gregtechceu.gtceu.client.TooltipsHandler;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "mezz.jei.forge.platform.FluidHelper", remap = false)
public class JeiFluidTooltipMixin {

    @Inject(
            method = "getTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraft/world/item/TooltipFlag;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0
    )
    private void gtextras$appendGtFluidTooltips(ITooltipBuilder tooltip, FluidStack stack, TooltipFlag flag, CallbackInfo ci) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try {
            TooltipsHandler.appendFluidTooltips(stack, (Component line) -> tooltip.add(line), flag);
        } catch (Throwable t) {
            // GT tooltip data is optional here; JEI keeps its own tooltip
        }
    }
}
