package com.gtextras.gt.trait;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class AmountFluidHandler extends NotifiableRecipeHandlerTrait<FluidIngredient> {

    private final AmountContainer container;

    public AmountFluidHandler(MetaMachine machine, AmountContainer container) {
        super(machine);
        this.container = container;
    }

    private double ratioOf(FluidIngredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) return 0;
        for (ExtrasFluids.Entry entry : container.fluidChoices()) {
            if (ingredient.test(new FluidStack(entry.fluid(), 1))) return entry.ratio();
        }
        return 0;
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left,
                                                   boolean simulate) {
        if (io != container.io()) return left;
        List<FluidIngredient> rest = new ArrayList<>();
        for (FluidIngredient ingredient : left) {
            double ratio = ratioOf(ingredient);
            if (ratio <= 0) {
                rest.add(ingredient);
                continue;
            }
            int units = (int) Math.min(Integer.MAX_VALUE, Math.ceil(ingredient.getAmount() * ratio));
            List<Integer> remaining = container.handleRecipeInner(io, recipe, new ArrayList<>(List.of(units)),
                    simulate);
            if (remaining == null || remaining.isEmpty() || remaining.get(0) == null || remaining.get(0) <= 0) {
                continue;
            }
            FluidIngredient copy = ingredient.copy();
            copy.setAmount((int) Math.min(Integer.MAX_VALUE, Math.ceil(remaining.get(0) / ratio)));
            rest.add(copy);
        }
        return rest.isEmpty() ? null : rest;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ArrayList<>();
        if (container.io() != IO.IN) return contents;
        int stored = container.getStored();
        if (stored <= 0) return contents;
        for (ExtrasFluids.Entry entry : container.fluidChoices()) {
            if (entry.ratio() <= 0) continue;
            int amount = (int) Math.min(Integer.MAX_VALUE, Math.floor(stored / entry.ratio()));
            if (amount > 0) contents.add(new FluidStack(entry.fluid(), amount));
        }
        return contents;
    }

    @Override
    public double getTotalContentAmount() {
        if (container.io() != IO.IN) return 0;
        return container.getStored();
    }

    @Override
    public RecipeCapability<FluidIngredient> getCapability() {
        return FluidRecipeCapability.CAP;
    }

    @Override
    public IO getHandlerIO() {
        return container.io();
    }
}
