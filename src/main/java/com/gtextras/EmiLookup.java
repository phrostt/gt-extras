package com.gtextras;

import com.gtextras.gt.NeedsReader;
import com.gtextras.gt.trait.ExtrasFluids;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public final class EmiLookup {

    private EmiLookup() {}

    public static List<EmiIngredient> inputs(EmiRecipe recipe) {
        List<EmiIngredient> out = new ArrayList<>();
        collect(recipe, true, out);
        return out;
    }

    public static List<EmiStack> outputs(EmiRecipe recipe) {
        List<EmiStack> out = new ArrayList<>();
        collect(recipe, false, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private static <T extends EmiIngredient> void collect(EmiRecipe recipe, boolean input, List<T> out) {
        if (!ModList.get().isLoaded("gtceu")) {
            return;
        }
        try {
            Object backing = EmiRecipeData.backingRecipeOf(recipe);
            NeedsReader.Needs needs = backing == null ? null : NeedsReader.read(backing);
            if (needs == null) {
                return;
            }
            boolean mana = input ? needs.manaIn() + needs.manaInTick() > 0 : needs.manaOut() + needs.manaOutTick() > 0;
            boolean source = input ? needs.sourceIn() + needs.sourceInTick() > 0
                    : needs.sourceOut() + needs.sourceOutTick() > 0;
            boolean blood = input ? needs.bloodIn() + needs.bloodInTick() > 0
                    : needs.bloodOut() + needs.bloodOutTick() > 0;
            if (mana) add(ExtrasFluids.mana(), out);
            if (source) add(ExtrasFluids.source(), out);
            if (blood) add(ExtrasFluids.blood(), out);
            for (int i = 0; i < needs.will().length; i++) {
                long[] w = needs.will()[i];
                boolean has = input ? w[0] + w[1] > 0 : w[2] + w[3] > 0;
                if (has) add(ExtrasFluids.will(i), out);
            }
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends EmiIngredient> void add(List<ExtrasFluids.Entry> entries, List<T> out) {
        for (ExtrasFluids.Entry entry : entries) {
            out.add((T) EmiStack.of(entry.fluid(), 1));
        }
    }
}
