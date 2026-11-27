package com.gtextras.mixin;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gtextras.gt.stage.StageData;
import com.gtextras.gt.stage.StageRuntime;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(value = RecipeLogic.class, remap = false)
public abstract class StageRecipeLogicMixin {

    @Shadow
    protected GTRecipe lastRecipe;
    @Shadow
    protected int progress;

    @Shadow
    protected abstract ActionResult handleRecipeIO(GTRecipe recipe, IO io);

    @Unique
    private int gtextras$stage;

    @Inject(method = "onRecipeFinish", at = @At("HEAD"), cancellable = true, require = 0)
    private void gtextras$stageFinish(CallbackInfo ci) {
        GTRecipe recipe = this.lastRecipe;
        if (recipe == null || !StageData.has(recipe.data)) {
            return;
        }
        ListTag stages = StageData.list(recipe.data);
        if (gtextras$stage >= stages.size()) {
            gtextras$stage = 0;
        }
        CompoundTag stage = stages.getCompound(gtextras$stage);
        int multiplier = StageRuntime.multiplier(recipe);
        double chance = stage.getDouble("chance");
        boolean success = chance >= 100.0 || ThreadLocalRandom.current().nextDouble() * 100.0 < chance;
        if (success) {
            gtextras$pay(StageData.specs(stage, "success"), multiplier);
            gtextras$stage++;
            if (gtextras$stage < stages.size()) {
                this.progress = 0;
                ci.cancel();
                return;
            }
            gtextras$stage = 0;
            return;
        }
        if (stage.getBoolean("repeat")) {
            this.progress = 0;
            ci.cancel();
            return;
        }
        gtextras$pay(StageData.specs(stage, "fail"), multiplier);
        gtextras$stage = 0;
    }

    @Unique
    private void gtextras$pay(java.util.List<String> specs, int multiplier) {
        GTRecipe payout = StageRuntime.payout(specs, multiplier);
        if (payout != null) {
            this.handleRecipeIO(payout, IO.OUT);
        }
    }

    @Inject(method = "handleRecipeIO", at = @At("HEAD"), cancellable = true, require = 0)
    private void gtextras$skipStagedOutputs(GTRecipe recipe, IO io, CallbackInfoReturnable<ActionResult> cir) {
        if (io == IO.OUT && recipe != null && StageData.has(recipe.data)) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Inject(method = "resetRecipeLogic", at = @At("HEAD"), require = 0)
    private void gtextras$resetStage(CallbackInfo ci) {
        gtextras$stage = 0;
    }

    @Inject(method = "interruptRecipe", at = @At("HEAD"), require = 0)
    private void gtextras$interruptStage(CallbackInfo ci) {
        gtextras$stage = 0;
    }

    @Inject(method = "saveCustomPersistedData", at = @At("TAIL"), require = 0)
    private void gtextras$saveStage(CompoundTag tag, boolean forDrop, CallbackInfo ci) {
        tag.putInt("gtx_stage", gtextras$stage);
    }

    @Inject(method = "loadCustomPersistedData", at = @At("TAIL"), require = 0)
    private void gtextras$loadStage(CompoundTag tag, CallbackInfo ci) {
        gtextras$stage = tag.getInt("gtx_stage");
    }

    @Inject(method = "hasCustomProgressLine", at = @At("HEAD"), cancellable = true, require = 0)
    private void gtextras$hasLine(CallbackInfoReturnable<Boolean> cir) {
        if (this.lastRecipe != null && StageData.has(this.lastRecipe.data)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getCustomProgressLine", at = @At("HEAD"), cancellable = true, require = 0)
    private void gtextras$line(CallbackInfoReturnable<Component> cir) {
        if (this.lastRecipe != null && StageData.has(this.lastRecipe.data)) {
            int total = StageData.list(this.lastRecipe.data).size();
            cir.setReturnValue(Component.literal("Stage " + (Math.min(gtextras$stage, total - 1) + 1) + "/" + total));
        }
    }
}
