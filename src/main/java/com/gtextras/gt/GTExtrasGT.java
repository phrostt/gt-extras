package com.gtextras.gt;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.gtextras.GTExtras;
import com.gtextras.gt.mana.ManaCaps;
import com.gtextras.gt.rotation.MinRpmCondition;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;

import java.util.function.Consumer;

public final class GTExtrasGT {

    public static GTRegistrate REGISTRATE;

    private static boolean botania;
    private static boolean ars;
    private static boolean create;
    private static boolean blood;

    private GTExtrasGT() {}

    @SuppressWarnings({ "rawtypes" })
    public static void init(IEventBus modBus) {
        ModList mods = ModList.get();
        botania = mods.isLoaded("botania");
        ars = mods.isLoaded("ars_nouveau");
        create = mods.isLoaded("create");
        blood = mods.isLoaded("bloodmagic");

        com.gtextras.gt.trait.ExtrasFluids.register();
        com.gtextras.gt.trait.HatchConfig.register();
        REGISTRATE = GTRegistrate.create(GTExtras.MOD_ID);
        REGISTRATE.registerRegistrate();

        modBus.addGenericListener(MachineDefinition.class,
                (GTCEuAPI.RegisterEvent<?, MachineDefinition> event) -> registerMachines());
        if (create) {
            modBus.addGenericListener(RecipeConditionType.class,
                    (GTCEuAPI.RegisterEvent<?, RecipeConditionType> event) -> registerConditions());
        }
        if (create) {
            com.gtextras.gt.rotation.coupler.CouplerRegistry.init(modBus);
            if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
                modBus.addListener(com.gtextras.gt.rotation.coupler.client.MotorClient::register);
            }
        }
        if (botania) {
            com.gtextras.gt.mana.ManaCaps.init();
        }
        GTExtras.LOGGER.info("[{}] GregTech hatches enabled (botania={}, ars_nouveau={}, create={})",
                GTExtras.MOD_ID, botania, ars, create);
    }

    private static void registerMachines() {
        REGISTRATE.creativeModeTab(com.gregtechceu.gtceu.common.data.GTCreativeModeTabs.MACHINE);
        if (botania) ExtrasMachines.initMana();
        if (ars) ExtrasMachines.initSource();
        if (blood) ExtrasMachines.initBlood();
        if (create) ExtrasMachines.initRotation();
    }

    private static void registerConditions() {
        ExtrasConditions.MIN_RPM = GTRegistries.RECIPE_CONDITIONS.register("gtextras_min_rpm",
                new RecipeConditionType<>(MinRpmCondition::new, MinRpmCondition.CODEC));
    }
}
