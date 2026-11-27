package com.gtextras.gt.stage;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public final class StageRuntime {

    private StageRuntime() {
    }

    public static int multiplier(GTRecipe recipe) {
        int a = Math.max(1, recipe.parallels);
        int b = Math.max(1, recipe.batchParallels);
        return a * b;
    }

    public static GTRecipe payout(java.util.List<String> specs, int multiplier) {
        if (specs == null || specs.isEmpty()) {
            return null;
        }
        GTRecipeBuilder builder = GTRecipeBuilder.ofRaw();
        boolean any = false;
        for (String spec : specs) {
            if (StageData.isFluid(spec)) {
                FluidStack fluid = StageData.fluid(spec, multiplier);
                if (!fluid.isEmpty()) {
                    builder.outputFluids(fluid);
                    any = true;
                }
            } else {
                ItemStack stack = StageData.item(spec, multiplier);
                if (!stack.isEmpty()) {
                    builder.outputItems(stack);
                    any = true;
                }
            }
        }
        return any ? builder.buildRawRecipe() : null;
    }

    public static String line(CompoundTag stage, int index) {
        boolean repeat = stage.getBoolean("repeat");
        java.util.List<String> items = StageData.specs(stage, "success");
        String item = items.isEmpty() ? null : name(items.get(0)) + (items.size() > 1 ? " +" + (items.size() - 1) : "");
        double chance = stage.getDouble("chance");
        String pct = chance == Math.rint(chance) ? String.valueOf((int) chance) : String.valueOf(chance);
        String text = (index + 1) + ": " + pct + "%%";
        if (item != null) {
            text += " " + item;
        }
        if (repeat) {
            text += item != null ? " (retry)" : " retry";
        }
        return text;
    }

    private static String name(String spec) {
        String id = StageData.id(spec);
        String path = id.substring(id.indexOf(':') + 1).replace('_', ' ');
        return path.length() > 12 ? path.substring(0, 12) : path;
    }

    public static int lineCount(ListTag stages) {
        return stages.size();
    }
}
