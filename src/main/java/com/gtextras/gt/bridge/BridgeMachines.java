package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import com.gtextras.GTExtras;
import com.gtextras.gt.GTExtrasGT;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public final class BridgeMachines {

    public static final int MIN_TIER = GTValues.LV;
    public static final int MAX_TIER = GTValues.UV;

    private BridgeMachines() {}

    public static void init() {
        for (BridgeDef def : BridgeRegistry.all()) {
            for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
                final int fixedTier = tier;
                hatch(def, tier, "input", IO.IN, BridgeAbilities.input(def.id), List.of(
                        Component.literal("Receives packets from linked " + def.displayName + " output hatches."),
                        Component.literal("Stores incoming packets for recipes and holds resonance."),
                        Component.literal("Resonance capacity: " + String.format("%,d",
                                (long) def.resonanceCap * Math.max(1, fixedTier - GTValues.LV + 1)))));
                hatch(def, tier, "output", IO.OUT, BridgeAbilities.output(def.id), List.of(
                        Component.literal("Sends packets produced by recipes to linked " + def.displayName
                                + " input hatches."),
                        Component.literal("Link it with the Link Tool, up to " + def.range + " blocks away.")));
            }
        }
    }

    private static void hatch(BridgeDef def, int tier, String kind, IO io, PartAbility ability,
                              List<Component> tooltip) {
        String name = def.id + "_" + kind + "_hatch";
        String id = name + "_" + GTValues.VN[tier].toLowerCase();
        Function<IMachineBlockEntity, MetaMachine> factory = holder -> new BridgeHatchMachine(holder, io, tier, def);
        GTExtrasGT.REGISTRATE.machine(id, factory)
                .langValue(GTValues.VNF[tier] + " " + def.displayName + " " + (kind.equals("input") ? "Input" : "Output")
                        + " Hatch")
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(ability)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .overlayTieredHullModel(new ResourceLocation(GTExtras.MOD_ID, "block/machine/part/" + name))
                .tooltipBuilder((stack, lines) -> lines.addAll(tooltip))
                .register();
    }
}
