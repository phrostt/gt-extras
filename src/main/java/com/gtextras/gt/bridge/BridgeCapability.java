package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.capability.recipe.IRecipeCapabilityHolder;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.MapIngredientTypeManager;

import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public final class BridgeCapability extends RecipeCapability<String> {

    public static final BridgeCapability CAP = new BridgeCapability();

    private BridgeCapability() {
        super("bridge", 0xFF6FD3FF, false, 30, BridgeSerializer.INSTANCE);
    }

    public static void registerLookup() {
        MapIngredientTypeManager.registerMapIngredient(String.class, MapBridgeIngredient::from);
    }

    @Override
    public boolean isRecipeSearchFilter() {
        return true;
    }

    @Override
    public boolean doAddGuiSlots() {
        return false;
    }

    @Override
    public List<Object> compressIngredients(Collection<Object> ingredients) {
        LinkedHashSet<String> channels = new LinkedHashSet<>();
        for (Object ingredient : ingredients) {
            String text = String.valueOf(ingredient).trim();
            if (text.isEmpty()) continue;
            int space = text.indexOf(' ');
            channels.add(space < 0 ? text : text.substring(0, space));
        }
        return new ArrayList<>(channels);
    }

    @Override
    public List<AbstractMapIngredient> getDefaultMapIngredient(Object object) {
        return MapBridgeIngredient.from(String.valueOf(object));
    }

    @Override
    public String copyInner(String content) {
        return content;
    }

    @Override
    public String copyWithModifier(String content, ContentModifier modifier) {
        try {
            BridgePacket packet = BridgePacket.parse(content);
            long scaled = Math.round(modifier.apply((double) packet.amount()));
            return packet.withAmount(scaled).toString();
        } catch (IllegalArgumentException e) {
            return content;
        }
    }

    @Override
    public void addXEIInfo(WidgetGroup group, int xOffset, GTRecipe recipe, List<Content> contents, boolean perTick,
                           boolean isInput, MutableInt yOffset) {
    }

    @Override
    public int getMaxParallelByInput(IRecipeCapabilityHolder holder, GTRecipe recipe, int limit, boolean tick) {
        return limit;
    }

    @Override
    public int limitMaxParallelByOutput(IRecipeCapabilityHolder holder, GTRecipe recipe, int multiplier,
                                        boolean tick) {
        return multiplier;
    }
}
