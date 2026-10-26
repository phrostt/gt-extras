package com.gtextras.gt;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.ContentJS;

import com.gtextras.GTExtras;

import com.mojang.datafixers.util.Pair;

import dev.latvian.mods.kubejs.recipe.component.NumberComponent;

@GTAddon
public class ExtrasAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return GTExtrasGT.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return GTExtras.MOD_ID;
    }

    @Override
    public void registerRecipeCapabilities() {
        GTRegistries.RECIPE_CAPABILITIES.register(ExtrasCapabilities.MANA.name, ExtrasCapabilities.MANA);
        GTRegistries.RECIPE_CAPABILITIES.register(ExtrasCapabilities.SOURCE.name, ExtrasCapabilities.SOURCE);
        GTRegistries.RECIPE_CAPABILITIES.register(ExtrasCapabilities.BLOOD.name, ExtrasCapabilities.BLOOD);
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
        register(event, ExtrasCapabilities.MANA);
        register(event, ExtrasCapabilities.SOURCE);
        register(event, ExtrasCapabilities.BLOOD);
    }

    private static void register(KJSRecipeKeyEvent event, AmountRecipeCapability capability) {
        ContentJS<Integer> in = new ContentJS<>(NumberComponent.INT, capability, false);
        ContentJS<Integer> out = new ContentJS<>(NumberComponent.INT, capability, true);
        event.registerKey(capability, Pair.<ContentJS<?>, ContentJS<?>>of(in, out));
    }
}
