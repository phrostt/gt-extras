package com.gtextras.gt;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.gtextras.gt.rotation.MinRpmCondition;

import java.util.List;
import java.util.Map;

public final class NeedsReader {

    public record Needs(long manaIn, long manaInTick, long manaOut, long manaOutTick,
                        long sourceIn, long sourceInTick, long sourceOut, long sourceOutTick,
                        long bloodIn, long bloodInTick, long bloodOut, long bloodOutTick, int rpm, long[][] will) {
        public boolean hasWill(int index) {
            long[] w = will[index];
            return w[0] + w[1] + w[2] + w[3] > 0;
        }

        public boolean hasAnyWill() {
            for (int i = 0; i < will.length; i++) {
                if (hasWill(i)) return true;
            }
            return false;
        }

        public boolean hasMana() {
            return manaIn + manaInTick + manaOut + manaOutTick > 0;
        }

        public boolean hasSource() {
            return sourceIn + sourceInTick + sourceOut + sourceOutTick > 0;
        }

        public boolean hasBlood() {
            return bloodIn + bloodInTick + bloodOut + bloodOutTick > 0;
        }

        public boolean hasRpm() {
            return rpm > 0;
        }
    }

    private NeedsReader() {}

    public static Needs read(Object recipeObject) {
        if (!(recipeObject instanceof GTRecipe recipe)) {
            return null;
        }
        int rpm = 0;
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition instanceof MinRpmCondition min) {
                rpm = Math.max(rpm, min.getRpm());
            }
        }
        long[][] will = new long[ExtrasCapabilities.WILL.length][];
        for (int i = 0; i < will.length; i++) {
            will[i] = new long[] { sum(recipe.inputs, ExtrasCapabilities.WILL[i]),
                    sum(recipe.tickInputs, ExtrasCapabilities.WILL[i]), sum(recipe.outputs, ExtrasCapabilities.WILL[i]),
                    sum(recipe.tickOutputs, ExtrasCapabilities.WILL[i]) };
        }
        Needs needs = new Needs(
                sum(recipe.inputs, ExtrasCapabilities.MANA), sum(recipe.tickInputs, ExtrasCapabilities.MANA),
                sum(recipe.outputs, ExtrasCapabilities.MANA), sum(recipe.tickOutputs, ExtrasCapabilities.MANA),
                sum(recipe.inputs, ExtrasCapabilities.SOURCE), sum(recipe.tickInputs, ExtrasCapabilities.SOURCE),
                sum(recipe.outputs, ExtrasCapabilities.SOURCE), sum(recipe.tickOutputs, ExtrasCapabilities.SOURCE),
                sum(recipe.inputs, ExtrasCapabilities.BLOOD), sum(recipe.tickInputs, ExtrasCapabilities.BLOOD),
                sum(recipe.outputs, ExtrasCapabilities.BLOOD), sum(recipe.tickOutputs, ExtrasCapabilities.BLOOD),
                rpm, will);
        return needs.hasMana() || needs.hasSource() || needs.hasBlood() || needs.hasRpm() || needs.hasAnyWill()
                ? needs : null;
    }

    private static long sum(Map<RecipeCapability<?>, List<Content>> map, RecipeCapability<?> capability) {
        if (map == null) return 0;
        List<Content> contents = map.get(capability);
        if (contents == null) return 0;
        long total = 0;
        for (Content content : contents) {
            if (content.getContent() instanceof Number n) total += n.longValue();
        }
        return total;
    }
}
