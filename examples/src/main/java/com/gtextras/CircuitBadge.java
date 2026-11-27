package com.gtextras;

import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public final class CircuitBadge {

    private CircuitBadge() {
    }

    public static boolean active(CoilConfig config) {
        return config != null && config.enabled && config.showExtraBadges && config.showCircuitBadge
                && (config.jeiEnabled || net.minecraftforge.fml.ModList.get().isLoaded("emi"));
    }

    public static ItemStack stackOf(Object content) {
        try {
            Object raw = content instanceof Content c ? c.content : content;
            if (raw instanceof SizedIngredient sized) {
                raw = sized.getInner();
            }
            if (!(raw instanceof Ingredient ingredient)) {
                return ItemStack.EMPTY;
            }
            ItemStack[] items = ingredient.getItems();
            if (items.length != 1) {
                return ItemStack.EMPTY;
            }
            ItemStack stack = items[0];
            if (stack.isEmpty() || !IntCircuitBehaviour.isIntegratedCircuit(stack)) {
                return ItemStack.EMPTY;
            }
            ItemStack copy = stack.copy();
            copy.setCount(1);
            return copy;
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack find(Object contents) {
        if (!(contents instanceof List<?> list)) {
            return ItemStack.EMPTY;
        }
        for (Object entry : list) {
            ItemStack stack = stackOf(entry);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack find(com.gregtechceu.gtceu.api.recipe.GTRecipe recipe) {
        try {
            return find(recipe.getInputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP));
        } catch (Throwable t) {
            return ItemStack.EMPTY;
        }
    }

    public static Object strip(Object contents) {
        try {
            if (!(contents instanceof List<?> list) || !active(CoilConfig.get())) {
                return contents;
            }
            boolean found = false;
            for (Object entry : list) {
                if (!stackOf(entry).isEmpty()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return contents;
            }
            List<Object> kept = new ArrayList<>(list.size());
            for (Object entry : list) {
                if (stackOf(entry).isEmpty()) {
                    kept.add(entry);
                }
            }
            return kept;
        } catch (Throwable t) {
            return contents;
        }
    }
}
