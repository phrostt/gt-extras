package com.gtextras.gt.rotation;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
<<<<<<< HEAD
=======
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import com.gtextras.gt.ExtrasConditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;

public class MinRpmCondition extends RecipeCondition<MinRpmCondition> {

    public static final Codec<MinRpmCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition
            .isReverse(instance)
            .and(Codec.INT.fieldOf("rpm").forGetter(MinRpmCondition::getRpm))
            .apply(instance, MinRpmCondition::new));

    private int rpm;

    public MinRpmCondition() {
        this(false, 0);
    }

    public MinRpmCondition(int rpm) {
        this(false, rpm);
    }

    public MinRpmCondition(boolean isReverse, int rpm) {
        super(isReverse);
        this.rpm = rpm;
    }

    public int getRpm() {
        return rpm;
    }

    @Override
    public RecipeConditionType<MinRpmCondition> getType() {
        return ExtrasConditions.MIN_RPM;
    }

    @Override
    public String getTranslationKey() {
        return "gtextras.recipe.condition.min_rpm";
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(getTranslationKey(), rpm);
    }

    @Override
    protected boolean testCondition(GTRecipe recipe, RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
<<<<<<< HEAD
        float best = 0.0f;
        boolean inputs = false;
        boolean outputs = false;
        if (machine instanceof IMultiController controller && controller.getMultiblockState() != null) {
            net.minecraft.world.level.Level level = machine.getLevel();
            java.util.Set<net.minecraft.core.BlockPos> seen = new java.util.HashSet<>();
            if (level != null) {
                for (net.minecraft.core.BlockPos base : controller.getMultiblockState().getCache()) {
                    for (int i = -1; i < net.minecraft.core.Direction.values().length; i++) {
                        net.minecraft.core.BlockPos pos = i < 0 ? base
                                : base.relative(net.minecraft.core.Direction.values()[i]);
                        if (!seen.add(pos.immutable())) continue;
                        if (level.getBlockEntity(pos) instanceof RotationInterfaceBlockEntity ri) {
                            if (ri.isOutput()) {
                                outputs = true;
                            } else {
                                inputs = true;
                                if (!ri.isOverStressed()) best = Math.max(best, Math.abs(ri.getSpeed()));
                            }
                        }
=======
        boolean hasReader = false;
        float best = 0.0f;
        if (machine instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof RotationReaderMachine reader) {
                    hasReader = true;
                    best = Math.max(best, Math.abs(reader.getRpm()));
                    if (reader.getRpm() >= rpm) {
                        return true;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
                    }
                }
            }
        }
<<<<<<< HEAD
        if (inputs && best >= rpm) return true;
        Component reason;
        if (inputs) {
            reason = Component.translatable("gtextras.recipe.rotation_too_slow", rpm, String.format("%.0f", best));
        } else if (outputs) {
            reason = Component.translatable("gtextras.recipe.rotation_wrong_mode_input", rpm);
        } else {
            reason = Component.translatable("gtextras.recipe.rotation_no_reader", rpm);
        }
=======
        Component reason = hasReader
                ? Component.translatable("gtextras.recipe.rotation_too_slow", rpm, String.format("%.0f", best))
                : Component.translatable("gtextras.recipe.rotation_no_reader", rpm);
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
        RecipeLogic.putFailureReason(recipeLogic, recipe, reason);
        return false;
    }

    @Override
    public MinRpmCondition createTemplate() {
        return new MinRpmCondition();
    }
}
