package com.gtextras.gt;

public final class ExtrasCapabilities {

    public static final AmountRecipeCapability MANA = new AmountRecipeCapability("mana", 0xFF2EC7F0, 20,
            "gtextras.recipe.mana");
    public static final AmountRecipeCapability SOURCE = new AmountRecipeCapability("source", 0xFFB45CFF, 21,
            "gtextras.recipe.source");

    public static final AmountRecipeCapability BLOOD = new AmountRecipeCapability("blood", 0xFFB01010, 22,
            "gtextras.recipe.blood");

    private ExtrasCapabilities() {}
}
