package com.gtextras.gt;

import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import com.gtextras.gt.durability.DamageItemCondition;
import com.gtextras.gt.rotation.MinRpmCondition;
import com.gtextras.gt.rotation.OutputRpmCondition;

public final class ExtrasConditions {

    public static RecipeConditionType<MinRpmCondition> MIN_RPM;
    public static RecipeConditionType<OutputRpmCondition> OUTPUT_RPM;

    public static RecipeConditionType<DamageItemCondition> DAMAGE_ITEM;

    private ExtrasConditions() {}
}
