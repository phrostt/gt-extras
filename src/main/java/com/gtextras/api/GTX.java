package com.gtextras.api;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;

import com.gtextras.gt.ExtrasAbilities;
import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.rotation.MinRpmCondition;

public final class GTX {

    public static final RecipeCapability<Integer> MANA = ExtrasCapabilities.MANA;
    public static final RecipeCapability<Integer> SOURCE = ExtrasCapabilities.SOURCE;
    public static final RecipeCapability<Integer> BLOOD = ExtrasCapabilities.BLOOD;

    public static final PartAbility MANA_INPUT = ExtrasAbilities.MANA_INPUT;
    public static final PartAbility MANA_OUTPUT = ExtrasAbilities.MANA_OUTPUT;
    public static final PartAbility SOURCE_INPUT = ExtrasAbilities.SOURCE_INPUT;
    public static final PartAbility SOURCE_OUTPUT = ExtrasAbilities.SOURCE_OUTPUT;
    public static final PartAbility BLOOD_INPUT = ExtrasAbilities.BLOOD_INPUT;
    public static final PartAbility BLOOD_OUTPUT = ExtrasAbilities.BLOOD_OUTPUT;
    public static final PartAbility ROTATION_READER = ExtrasAbilities.ROTATION_READER;

    private GTX() {}

    public static RecipeCondition<?> minRpm(int rpm) {
        return new MinRpmCondition(rpm);
    }
}
