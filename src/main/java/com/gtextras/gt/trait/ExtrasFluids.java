package com.gtextras.gt.trait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class ExtrasFluids {

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOOD;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MANA;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOURCE;
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Fluids the hatches use with adjacent tanks, 1 mB = 1 unit. Each entry is a fluid id (modid:name) or a fluid tag written with a leading # (#forge:blood).",
                "Input hatches accept every fluid in the list. Output hatches push the entry chosen by sneak-right-clicking them with a screwdriver, starting from the first.");
        BLOOD = builder.defineListAllowEmpty("bloodFluids",
                List.of("bloodmagic:life_essence_fluid", "evilcraft:blood", "gtceu:sanguine_concentrate", "#forge:blood"),
                o -> o instanceof String);
        MANA = builder.defineListAllowEmpty("manaFluids", List.of("gtceu:mana_essence", "#forge:mana"), o -> o instanceof String);
        SOURCE = builder.defineListAllowEmpty("sourceFluids", List.of("gtceu:source", "#forge:source"), o -> o instanceof String);
        SPEC = builder.build();
    }

    private ExtrasFluids() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "gtextras-fluids.toml");
    }

    public record Entry(Fluid fluid, double ratio) {}

    public static List<Entry> blood() {
        return resolve(BLOOD.get());
    }

    public static List<Entry> mana() {
        return resolve(MANA.get());
    }

    public static List<Entry> source() {
        return resolve(SOURCE.get());
    }

    private static List<Entry> resolve(List<? extends String> entries) {
        java.util.LinkedHashMap<Fluid, Double> out = new java.util.LinkedHashMap<>();
        for (String entry : entries) {
            if (entry == null) continue;
            String text = entry.trim();
            double ratio = 1.0;
            int eq = text.indexOf('=');
            if (eq >= 0) {
                try {
                    ratio = Double.parseDouble(text.substring(eq + 1).trim());
                } catch (NumberFormatException e) {
                    continue;
                }
                text = text.substring(0, eq).trim();
            }
            if (ratio <= 0) continue;
            boolean tag = text.startsWith("#");
            ResourceLocation rl = ResourceLocation.tryParse(tag ? text.substring(1) : text);
            if (rl == null) continue;
            if (tag) {
                net.minecraft.tags.TagKey<Fluid> key = net.minecraft.tags.TagKey.create(
                        net.minecraft.core.registries.Registries.FLUID, rl);
                var tags = ForgeRegistries.FLUIDS.tags();
                if (tags == null) continue;
                for (Fluid fluid : tags.getTag(key)) {
                    if (fluid != Fluids.EMPTY && fluid.isSource(fluid.defaultFluidState())) {
                        out.putIfAbsent(fluid, ratio);
                    }
                }
            } else {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
                if (fluid != null && fluid != Fluids.EMPTY) out.putIfAbsent(fluid, ratio);
            }
        }
        List<Entry> list = new ArrayList<>(out.size());
        out.forEach((fluid, ratio) -> list.add(new Entry(fluid, ratio)));
        return list;
    }

    private static IFluidHandler neighbour(Level level, BlockPos pos, Direction dir) {
        BlockEntity be = level.getBlockEntity(pos.relative(dir));
        if (be == null || be instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity) return null;
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, dir.getOpposite()).resolve().orElse(null);
    }

    public static int pull(Level level, BlockPos pos, List<Entry> fluids, int maxUnits) {
        int units = 0;
        if (fluids.isEmpty() || maxUnits <= 0) return 0;
        for (Direction dir : Direction.values()) {
            if (units >= maxUnits) break;
            IFluidHandler handler = neighbour(level, pos, dir);
            if (handler == null) continue;
            for (Entry entry : fluids) {
                if (units >= maxUnits) break;
                int mb = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, (long) ((maxUnits - units) / entry.ratio())));
                FluidStack drained = handler.drain(new FluidStack(entry.fluid(), mb), IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty()) units += (int) Math.round(drained.getAmount() * entry.ratio());
            }
        }
        return units;
    }

    public static int push(Level level, BlockPos pos, List<Entry> fluids, int maxUnits, int index) {
        int units = 0;
        if (fluids.isEmpty() || maxUnits <= 0) return 0;
        Entry entry = fluids.get(Math.floorMod(index, fluids.size()));
        for (Direction dir : Direction.values()) {
            if (units >= maxUnits) break;
            IFluidHandler handler = neighbour(level, pos, dir);
            if (handler == null) continue;
            int mb = (int) Math.min(Integer.MAX_VALUE, (long) ((maxUnits - units) / entry.ratio()));
            if (mb <= 0) break;
            int filled = handler.fill(new FluidStack(entry.fluid(), mb), IFluidHandler.FluidAction.EXECUTE);
            units += (int) Math.round(filled * entry.ratio());
        }
        return units;
    }
}
