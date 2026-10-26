package com.gtextras.gt;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
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
    }
}
