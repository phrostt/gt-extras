package com.gtextras.gt.rotation;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
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
        return null;
    }

    @Override
    protected boolean testCondition(GTRecipe recipe, RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
        boolean hasReader = false;
        float best = 0.0f;
        if (machine instanceof IMultiController controller) {
            for (IMultiPart part : controller.getParts()) {
                if (part instanceof RotationReaderMachine reader) {
                    hasReader = true;
                    best = Math.max(best, Math.abs(reader.getRpm()));
                    if (reader.getRpm() >= rpm) {
                        return true;
                    }
                }
            }
        }
        Component reason = hasReader
                ? Component.translatable("gtextras.recipe.rotation_too_slow", rpm, String.format("%.0f", best))
                : Component.translatable("gtextras.recipe.rotation_no_reader", rpm);
        RecipeLogic.putFailureReason(recipeLogic, recipe, reason);
        return false;
    }

    @Override
    public MinRpmCondition createTemplate() {
        return new MinRpmCondition();
    }
}
