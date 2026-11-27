package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

public final class BridgeModifiers {

    private BridgeModifiers() {}

    public static double level(MetaMachine machine, String bridge, String data) {
        if (!(machine instanceof IMultiController controller)) return 0;
        String key = "data " + data;
        double best = 0;
        for (IMultiPart part : controller.getParts()) {
            MetaMachine meta = part.self();
            for (MachineTrait trait : meta.getTraits()) {
                if (trait instanceof BridgeContainer container && container.io() == IO.IN
                        && container.def().id.equals(bridge)) {
                    best = Math.max(best, container.fraction(key));
                }
            }
        }
        return best;
    }

    public static RecipeModifier yield(String bridge, String data, double maxBonus) {
        return (MetaMachine machine, GTRecipe recipe) -> {
            double fraction = level(machine, bridge, data);
            if (fraction <= 0 || maxBonus <= 0) return ModifierFunction.IDENTITY;
            return ModifierFunction.builder()
                    .outputModifier(ContentModifier.multiplier(1.0 + maxBonus * fraction))
                    .build();
        };
    }

    public static RecipeModifier speed(String bridge, String data, double maxBonus) {
        return (MetaMachine machine, GTRecipe recipe) -> {
            double fraction = level(machine, bridge, data);
            if (fraction <= 0 || maxBonus <= 0) return ModifierFunction.IDENTITY;
            double factor = Math.max(0.05, 1.0 - Math.min(0.95, maxBonus) * fraction);
            return ModifierFunction.builder().durationMultiplier(factor).build();
        };
    }
}
