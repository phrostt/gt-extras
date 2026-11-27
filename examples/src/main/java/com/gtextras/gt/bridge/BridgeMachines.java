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
            int[] ampList = def.energyEnabled && def.energyAmperages.length > 0 ? def.energyAmperages : new int[] { 0 };
            for (int tier : def.tiers) for (int amps : ampList) {
                final int fixedTier = tier;
                final String ampText = amps > 0 ? " Rated for " + amps + " A." : "";
                hatch(def, tier, amps, "input", IO.IN, BridgeAbilities.input(def.id), List.of(
                        Component.literal("Receives packets from linked " + def.displayName + " output hatches."),
                        Component.literal("Stores incoming packets for recipes and holds resonance."),
                        Component.literal(def.energyEnabled
                                ? "Emits energy received from linked output hatches to adjacent blocks at "
                                        + GTValues.VN[fixedTier] + "." + ampText
                                : "Energy transfer is disabled for this bridge."),
                        Component.literal("Resonance capacity: " + String.format("%,d",
                                (long) def.resonanceCap * Math.max(1, fixedTier - GTValues.LV + 1)))));
                hatch(def, tier, amps, "output", IO.OUT, BridgeAbilities.output(def.id), List.of(
                        Component.literal("Sends packets produced by recipes to linked " + def.displayName
                                + " input hatches."),
                        Component.literal("Link it with the Link Tool, up to " + def.maxLinkRange() + " blocks away."),
                        Component.literal(def.energyEnabled
                                ? "Accepts " + GTValues.VN[fixedTier] + " energy from cables and sends it to linked "
                                        + "input hatches up to " + def.energyRange + " blocks away"
                                        + (def.energyLossPercent > 0 ? " (" + def.energyLossPercent + "% loss)." : ".")
                                        + (def.energyAllowTierMismatch ? "" : " Only to hatches of the same tier.") + ampText
                                : "Energy transfer is disabled for this bridge.")));
            }
        }
    }

    private static void hatch(BridgeDef def, int tier, int amps, String kind, IO io, PartAbility ability,
                              List<Component> tooltip) {
        String name = def.id + "_" + kind + "_hatch";
        String id = name + "_" + GTValues.VN[tier].toLowerCase() + (amps > 0 ? "_" + amps + "a" : "");
        Function<IMachineBlockEntity, MetaMachine> factory = holder -> new BridgeHatchMachine(holder, io, tier, def, amps);
        GTExtrasGT.REGISTRATE.machine(id, factory)
                .langValue(GTValues.VNF[tier] + " " + def.displayName + " " + (kind.equals("input") ? "Input" : "Output")
                        + " Hatch" + (amps > 0 ? " (" + amps + "A)" : ""))
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(ability)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .overlayTieredHullModel(new ResourceLocation(GTExtras.MOD_ID, "block/machine/part/" + name))
                .tooltipBuilder((stack, lines) -> lines.addAll(tooltip))
                .register();
    }
}
