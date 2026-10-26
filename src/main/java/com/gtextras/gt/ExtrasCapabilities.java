package com.gtextras.gt;

public final class ExtrasCapabilities {

    public static final AmountRecipeCapability MANA = new AmountRecipeCapability("mana", 0xFF2EC7F0, 20,
            "gtextras.recipe.mana");
    public static final AmountRecipeCapability SOURCE = new AmountRecipeCapability("source", 0xFFB45CFF, 21,
            "gtextras.recipe.source");

    public static final AmountRecipeCapability BLOOD = new AmountRecipeCapability("blood", 0xFFB01010, 22,
            "gtextras.recipe.blood");

<<<<<<< HEAD
    public static final String[] WILL_NAMES = { "raw", "corrosive", "destructive", "vengeful", "steadfast" };
    private static final int[] WILL_COLORS = { 0xFFE8E8E8, 0xFF8BD83A, 0xFFE8892B, 0xFFE04A4A, 0xFF4FA3E8 };

    public static final AmountRecipeCapability[] WILL = new AmountRecipeCapability[WILL_NAMES.length];

    static {
        for (int i = 0; i < WILL_NAMES.length; i++) {
            WILL[i] = new AmountRecipeCapability("will_" + WILL_NAMES[i], WILL_COLORS[i], 23 + i,
                    "gtextras.recipe.will_" + WILL_NAMES[i]);
        }
    }

=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    private ExtrasCapabilities() {}
}
