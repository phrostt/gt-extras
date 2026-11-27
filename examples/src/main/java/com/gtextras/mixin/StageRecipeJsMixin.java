package com.gtextras.mixin;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ExtendedOutputItem;
import com.gtextras.gt.stage.StageData;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(value = GTRecipeSchema.GTRecipeJS.class, remap = false)
public abstract class StageRecipeJsMixin {

    @Unique
    private ListTag gtextras$stages = new ListTag();

    public GTRecipeSchema.GTRecipeJS stage(double percent, Object success, boolean repeat, Object fail) {
        GTRecipeSchema.GTRecipeJS self = (GTRecipeSchema.GTRecipeJS) (Object) this;
        List<String> successSpecs = gtextras$specs(success);
        List<String> failSpecs = gtextras$specs(fail);
        gtextras$stages.add(StageData.stage(percent, successSpecs, repeat, failSpecs));
        self.addData(StageData.KEY, gtextras$stages);

        List<double[]> cum = StageData.cumulative(gtextras$stages);
        double[] row = cum.get(cum.size() - 1);
        for (String spec : successSpecs) {
            gtextras$output(self, spec, row[1]);
        }
        if (!repeat) {
            for (String spec : failSpecs) {
                gtextras$output(self, spec, row[2]);
            }
        }
        return self;
    }

    @Unique
    private static List<String> gtextras$specs(Object value) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        if (value == null) {
            return out;
        }
        if (value instanceof java.util.Collection<?> c) {
            for (Object o : c) {
                gtextras$add(out, o);
            }
        } else if (value instanceof Object[] arr) {
            for (Object o : arr) {
                gtextras$add(out, o);
            }
        } else {
            gtextras$add(out, value);
        }
        return out;
    }

    @Unique
    private static void gtextras$add(List<String> out, Object o) {
        if (o == null) {
            return;
        }
        String s = String.valueOf(o).trim();
        if (!s.isEmpty() && !s.equals("null")) {
            out.add(s);
        }
    }

    @Unique
    private static void gtextras$output(GTRecipeSchema.GTRecipeJS self, String spec, double probability) {
        int chance = (int) Math.max(1, Math.min(10000, Math.round(probability * 10000.0)));
        if (StageData.isFluid(spec)) {
            FluidStackJS fluid = FluidStackJS.of(StageData.id(spec)).withAmount(StageData.amount(spec));
            self.chancedFluidOutput(fluid, chance, 0);
        } else {
            self.chancedOutput(ExtendedOutputItem.of(StageData.amount(spec) + "x " + StageData.id(spec)), chance, 0);
        }
    }
}
