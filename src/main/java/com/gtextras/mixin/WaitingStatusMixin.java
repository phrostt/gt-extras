package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = WorkableElectricMultiblockMachine.class, remap = false)
public abstract class WaitingStatusMixin {

    @Inject(method = "addDisplayText", at = @At("TAIL"), remap = false)
    private void gtextras$showWaiting(List<Component> textList, CallbackInfo ci) {
        WorkableElectricMultiblockMachine self = (WorkableElectricMultiblockMachine) (Object) this;
        if (!self.isFormed()) return;
        RecipeLogic logic = self.getRecipeLogic();
        if (logic == null || !logic.isWaiting()) return;
        Component reason = logic.getWaitingReason();
        if (reason == null) return;
        Component line = Component.translatable("gtextras.status.paused", reason).withStyle(ChatFormatting.GOLD);
        for (int i = 0; i < textList.size(); i++) {
            if (textList.get(i).getContents() instanceof TranslatableContents contents
                    && "gtceu.multiblock.running".equals(contents.getKey())) {
                textList.set(i, line);
                return;
            }
        }
        textList.add(line);
    }
}
