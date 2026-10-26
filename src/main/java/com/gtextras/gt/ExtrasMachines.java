package com.gtextras.gt;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;

import com.gtextras.GTExtras;
import com.gtextras.gt.blood.BloodContainer;
import com.gtextras.gt.blood.BloodHatchMachine;
import com.gtextras.gt.mana.ManaContainer;
import com.gtextras.gt.mana.ManaHatchMachine;
import com.gtextras.gt.rotation.RotationReaderMachine;
import com.gtextras.gt.source.SourceContainer;
import com.gtextras.gt.source.SourceHatchMachine;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public final class ExtrasMachines {

    public static final int MIN_TIER = GTValues.LV;
    public static final int MAX_TIER = GTValues.UV;

    public static final MachineDefinition[] MANA_INPUT_HATCH = new MachineDefinition[GTValues.TIER_COUNT];
    public static final MachineDefinition[] MANA_OUTPUT_HATCH = new MachineDefinition[GTValues.TIER_COUNT];
    public static final MachineDefinition[] SOURCE_INPUT_HATCH = new MachineDefinition[GTValues.TIER_COUNT];
    public static final MachineDefinition[] SOURCE_OUTPUT_HATCH = new MachineDefinition[GTValues.TIER_COUNT];
    public static final MachineDefinition[] BLOOD_INPUT_HATCH = new MachineDefinition[GTValues.TIER_COUNT];
    public static final MachineDefinition[] BLOOD_OUTPUT_HATCH = new MachineDefinition[GTValues.TIER_COUNT];
    public static final MachineDefinition[] ROTATION_READER_HATCH = new MachineDefinition[GTValues.TIER_COUNT];

    private ExtrasMachines() {}

    public static void initMana() {
        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            final int fixedTier = tier;
            java.util.function.Function<Boolean, java.util.List<Component>> tips = output -> tips("mana", "mana", ManaContainer.capacityFor(fixedTier), ManaContainer.transferFor(fixedTier), ManaContainer.INTERVAL, output);
            MANA_INPUT_HATCH[tier] = hatch("mana_input_hatch", "Mana Input Hatch", tier,
                    (holder, t) -> new ManaHatchMachine(holder, IO.IN, t), ExtrasAbilities.MANA_INPUT,
                    () -> tips.apply(false));
            MANA_OUTPUT_HATCH[tier] = hatch("mana_output_hatch", "Mana Output Hatch", tier,
                    (holder, t) -> new ManaHatchMachine(holder, IO.OUT, t), ExtrasAbilities.MANA_OUTPUT,
                    () -> tips.apply(true));
        }
    }

    public static void initSource() {
        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            final int fixedTier = tier;
            java.util.function.Function<Boolean, java.util.List<Component>> tips = output -> tips("source", "source", SourceContainer.capacityFor(fixedTier), SourceContainer.transferFor(fixedTier), SourceContainer.INTERVAL, output);
            SOURCE_INPUT_HATCH[tier] = hatch("source_input_hatch", "Source Input Hatch", tier,
                    (holder, t) -> new SourceHatchMachine(holder, IO.IN, t), ExtrasAbilities.SOURCE_INPUT,
                    () -> tips.apply(false));
            SOURCE_OUTPUT_HATCH[tier] = hatch("source_output_hatch", "Source Output Hatch", tier,
                    (holder, t) -> new SourceHatchMachine(holder, IO.OUT, t), ExtrasAbilities.SOURCE_OUTPUT,
                    () -> tips.apply(true));
        }
    }

    public static void initBlood() {
        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            final int fixedTier = tier;
            java.util.function.Function<Boolean, java.util.List<Component>> tips = output -> tips("blood", "LP", BloodContainer.capacityFor(fixedTier), BloodContainer.transferFor(fixedTier), BloodContainer.INTERVAL, output);
            BLOOD_INPUT_HATCH[tier] = hatch("blood_input_hatch", "Blood Input Hatch", tier,
                    (holder, t) -> new BloodHatchMachine(holder, IO.IN, t), ExtrasAbilities.BLOOD_INPUT,
                    () -> tips.apply(false));
            BLOOD_OUTPUT_HATCH[tier] = hatch("blood_output_hatch", "Blood Output Hatch", tier,
                    (holder, t) -> new BloodHatchMachine(holder, IO.OUT, t), ExtrasAbilities.BLOOD_OUTPUT,
                    () -> tips.apply(true));
        }
    }

    public static void initRotation() {
        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            ROTATION_READER_HATCH[tier] = hatch("rotation_reader_hatch", "Rotation Reader Hatch", tier,
                    RotationReaderMachine::new, ExtrasAbilities.ROTATION_READER,
                    () -> java.util.List.of(Component.translatable("gtextras.tooltip.rotation_reader")));
        }
    }

    private static java.util.List<Component> tips(String key, String unit, int capacity, int perOperation,
                                                  int interval, boolean output) {
        double perTick = (double) perOperation / interval;
        return java.util.List.of(
                Component.translatable("gtextras.tooltip.storage", String.format("%,d", capacity), unit),
                Component.translatable("gtextras.tooltip.rate", String.format("%,.0f", perTick), unit),
                Component.translatable("gtextras.tooltip." + key + (output ? "_output" : "_input")));
    }

    private interface TieredFactory {
        MetaMachine create(IMachineBlockEntity holder, int tier);
    }

    private static MachineDefinition hatch(String name, String lang, int tier, TieredFactory factory,
                                           PartAbility ability, java.util.function.Supplier<java.util.List<Component>> tooltip) {
        String id = name + "_" + GTValues.VN[tier].toLowerCase();
        Function<IMachineBlockEntity, MetaMachine> f = holder -> factory.create(holder, tier);
        return GTExtrasGT.REGISTRATE.machine(id, f)
                .langValue(GTValues.VNF[tier] + " " + lang)
                .tier(tier)
                .rotationState(RotationState.ALL)
                .abilities(ability)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .overlayTieredHullModel(new ResourceLocation(GTExtras.MOD_ID, "block/machine/part/" + name))
                .tooltipBuilder((stack, lines) -> lines.addAll(tooltip.get()))
                .register();
    }
}
