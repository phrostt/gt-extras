package com.gtextras.gt.rotation;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import com.gtextras.gt.ExtrasConditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;

public class OutputRpmCondition extends RecipeCondition<OutputRpmCondition> {

    public static final Codec<OutputRpmCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition
            .isReverse(instance)
            .and(Codec.INT.fieldOf("rpm").forGetter(OutputRpmCondition::getRpm))
            .apply(instance, OutputRpmCondition::new));

    private int rpm;

    public OutputRpmCondition() {
        this(false, 0);
    }

    public OutputRpmCondition(int rpm) {
        this(false, rpm);
    }

    public OutputRpmCondition(boolean isReverse, int rpm) {
        super(isReverse);
        this.rpm = rpm;
    }

    public int getRpm() {
        return rpm;
    }

    @Override
    public RecipeConditionType<OutputRpmCondition> getType() {
        return ExtrasConditions.OUTPUT_RPM;
    }

    @Override
    public String getTranslationKey() {
        return "gtextras.recipe.condition.output_rpm";
    }

    @Override
    public Component getTooltips() {
        return Component.translatable(getTranslationKey(), Math.min(256, rpm));
    }

    @Override
    protected boolean testCondition(GTRecipe recipe, RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
        boolean inputs = false;
        if (machine instanceof IMultiController controller && controller.getMultiblockState() != null) {
            net.minecraft.world.level.Level level = machine.getLevel();
            if (level != null) {
                for (net.minecraft.core.BlockPos base : controller.getMultiblockState().getCache()) {
                    for (int i = -1; i < net.minecraft.core.Direction.values().length; i++) {
                        net.minecraft.core.BlockPos pos = i < 0 ? base
                                : base.relative(net.minecraft.core.Direction.values()[i]);
                        if (level.getBlockEntity(pos) instanceof RotationInterfaceBlockEntity ri) {
                            if (ri.isOutput()) return true;
                            inputs = true;
                        }
                    }
                }
            }
        }
        RecipeLogic.putFailureReason(recipeLogic, recipe, Component.translatable(inputs
                ? "gtextras.recipe.rotation_wrong_mode_output" : "gtextras.recipe.rotation_no_interface", rpm));
        return false;
    }

    @Override
    public OutputRpmCondition createTemplate() {
        return new OutputRpmCondition();
    }
}
