package com.gtextras.gt;

<<<<<<< HEAD
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gtextras.gt.trait.AmountContainer;
=======
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.content.SerializerInteger;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;
<<<<<<< HEAD
import java.util.Map;
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

public class AmountRecipeCapability extends RecipeCapability<Integer> {

    private final String langKey;

    public AmountRecipeCapability(String name, int color, int sortIndex, String langKey) {
        super(name, color, false, sortIndex, SerializerInteger.INSTANCE);
        this.langKey = langKey;
    }

    @Override
    public Integer copyInner(Integer content) {
        return content;
    }

    @Override
    public Integer copyWithModifier(Integer content, ContentModifier modifier) {
        return modifier.apply(content);
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
<<<<<<< HEAD
    }

    private long perParallel(Map<RecipeCapability<?>, List<Content>> side) {
        List<Content> contents = side.get(this);
        long total = 0;
        if (contents != null) {
            for (Content content : contents) {
                total += of(content.getContent());
            }
        }
        return total;
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        long per = perParallel(tick ? recipe.tickInputs : recipe.inputs);
        if (per <= 0 || !holder.hasCapabilityProxies()) {
            return limit;
        }
        long available = 0;
        for (IRecipeHandler<?> handler : holder.getCapabilitiesFlat(IO.IN, this)) {
            if (handler instanceof AmountContainer container) {
                available += container.getStored();
            }
        }
        return (int) Math.max(1L, Math.min(limit, available / per));
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier, boolean tick) {
        long per = perParallel(tick ? recipe.tickOutputs : recipe.outputs);
        if (per <= 0 || !holder.hasCapabilityProxies()) {
            return multiplier;
        }
        long space = 0;
        for (IRecipeHandler<?> handler : holder.getCapabilitiesFlat(IO.OUT, this)) {
            if (handler instanceof AmountContainer container) {
                space += container.getSpace();
            }
        }
        return (int) Math.max(1L, Math.min(multiplier, space / per));
=======
        long total = 0;
        for (Content content : contents) {
            total += of(content.getContent());
        }
        if (total <= 0) return;
        String key = langKey + (isInput ? ".in" : ".out") + (perTick ? "_tick" : "");
        int below = 0;
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition.getTooltips() != null) {
                below++;
            }
        }
        group.addWidget(new LabelWidget(3 - xOffset, yOffset.addAndGet(10) + below * 10,
                LocalizationUtils.format(key, String.format("%,d", total))));
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    }
}
