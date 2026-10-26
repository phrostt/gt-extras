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
    private static final java.util.List<ForgeConfigSpec.ConfigValue<List<? extends String>>> WILL = new ArrayList<>();
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> WILL_ITEMS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MANA_ITEMS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> BLOOD_ITEMS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SOURCE_ITEMS;
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
        for (String type : com.gtextras.gt.ExtrasCapabilities.WILL_NAMES) {
            String material = type.equals("raw") ? "demon" : type;
            List<String> defaults = new ArrayList<>(List.of("gtceu:" + material, "#forge:" + material,
                    "gtceu:" + type + "_will", "#forge:" + type + "_will", "#forge:will/" + type));
            WILL.add(builder.defineListAllowEmpty(type + "WillFluids", defaults, o -> o instanceof String));
        }
        List<String> items = new ArrayList<>();
        String[] grades = { "chipped", "flawed", "flawless", "exquisite" };
        int[] values = { 36, 72, 288, 576 };
        String[] crystals = { "default", "corrosive", "destructive", "vengeful", "steadfast" };
        for (int t = 0; t < com.gtextras.gt.ExtrasCapabilities.WILL_NAMES.length; t++) {
            String type = com.gtextras.gt.ExtrasCapabilities.WILL_NAMES[t];
            String material = type.equals("raw") ? "demon" : type;
            for (int g = 0; g < grades.length; g++) {
                items.add("gtceu:" + grades[g] + "_" + material + "_gem=" + values[g] + ":" + type);
            }
            items.add("gtceu:" + material + "_gem=144:" + type);
            items.add("bloodmagic:" + crystals[t] + "crystal=144:" + type);
        }
        builder.comment("Extra items the Will Input Hatch can consume for will. Format: item_id_or_#tag=amount:type, where type is raw, corrosive, destructive, vengeful or steadfast.");
        WILL_ITEMS = builder.defineListAllowEmpty("willItems", items, o -> o instanceof String);
        builder.comment("Items the input hatches eat from adjacent inventories on top of GregTech items made of the material of a listed fluid. Format: item_id_or_#tag=amount.");
        MANA_ITEMS = builder.defineListAllowEmpty("manaItems", List.of(), o -> o instanceof String);
        BLOOD_ITEMS = builder.defineListAllowEmpty("bloodItems", List.of("evilcraft:condensed_blood=500"),
                o -> o instanceof String);
        SOURCE_ITEMS = builder.defineListAllowEmpty("sourceItems", List.of("ars_nouveau:source_gem=1000"),
                o -> o instanceof String);
        SPEC = builder.build();
    }

    private ExtrasFluids() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "gtextras-fluids.toml");
    }

    public record Entry(Fluid fluid, double ratio) {}

    public static List<Entry> blood() {
        return resolve(read(BLOOD));
    }

    public static List<Entry> mana() {
        return resolve(read(MANA));
    }

    public static List<Entry> source() {
        return resolve(read(SOURCE));
    }

    public record WillItem(String key, boolean tag, int amount, int type) {}

    public static List<WillItem> willItems() {
        List<? extends String> raw;
        try {
            raw = WILL_ITEMS.get();
        } catch (RuntimeException e) {
            raw = WILL_ITEMS.getDefault();
        }
        List<WillItem> out = new ArrayList<>();
        List<String> names = java.util.Arrays.asList(com.gtextras.gt.ExtrasCapabilities.WILL_NAMES);
        for (String line : raw) {
            try {
                int eq = line.lastIndexOf('=');
                int colon = line.lastIndexOf(':');
                String key = line.substring(0, eq).trim();
                int amount = Integer.parseInt(line.substring(eq + 1, colon).trim());
                int type = names.indexOf(line.substring(colon + 1).trim().toLowerCase(java.util.Locale.ROOT));
                if (type >= 0 && amount > 0 && !key.isEmpty()) {
                    boolean tag = key.startsWith("#");
                    out.add(new WillItem(tag ? key.substring(1) : key, tag, amount, type));
                }
            } catch (RuntimeException ignored) {
            }
        }
        return out;
    }

    public static List<Entry> will(int index) {
        List<Entry> list = resolve(read(WILL.get(index)));
        if (index != 0) {
            return list;
        }
        java.util.Set<Fluid> known = new java.util.HashSet<>();
        for (Entry entry : list) {
            known.add(entry.fluid());
        }
        for (int i = 1; i < WILL.size(); i++) {
            for (Entry entry : resolve(read(WILL.get(i)))) {
                known.add(entry.fluid());
            }
        }
        for (Entry entry : resolve(java.util.List.of("#forge:will"))) {
            if (known.add(entry.fluid())) {
                list.add(entry);
            }
        }
        return list;
    }

    public record ItemValue(String key, boolean tag, int amount) {}

    public static List<ItemValue> manaItems() {
        return items(MANA_ITEMS);
    }

    public static List<ItemValue> bloodItems() {
        return items(BLOOD_ITEMS);
    }

    public static List<ItemValue> sourceItems() {
        return items(SOURCE_ITEMS);
    }

    private static List<ItemValue> items(ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
        List<ItemValue> out = new ArrayList<>();
        for (String line : read(value)) {
            try {
                int eq = line.lastIndexOf('=');
                String key = line.substring(0, eq).trim();
                int amount = Integer.parseInt(line.substring(eq + 1).trim());
                if (amount > 0 && !key.isEmpty()) {
                    boolean tag = key.startsWith("#");
                    out.add(new ItemValue(tag ? key.substring(1) : key, tag, amount));
                }
            } catch (RuntimeException ignored) {
            }
        }
        return out;
    }

    private static int itemWorth(net.minecraft.world.item.ItemStack stack, List<Entry> fluids, List<ItemValue> extras) {
        for (ItemValue entry : extras) {
            ResourceLocation id = ResourceLocation.tryParse(entry.key());
            if (id == null) continue;
            if (entry.tag()) {
                if (stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, id))) {
                    return entry.amount();
                }
            } else if (id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                return entry.amount();
            }
        }
        try {
            var info = com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.getMaterialStack(stack);
            if (info == null || info.material() == null || info.amount() <= 0) return 0;
            Fluid fluid = info.material().getFluid();
            if (fluid == null || fluid == Fluids.EMPTY) return 0;
            for (Entry entry : fluids) {
                if (entry.fluid() == fluid) {
                    double mb = info.amount() * 144.0
                            / com.gregtechceu.gtceu.api.GTValues.M;
                    return (int) Math.min(Integer.MAX_VALUE, Math.floor(mb * entry.ratio()));
                }
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    public static int eat(Level level, BlockPos pos, List<Entry> fluids, List<ItemValue> extras, int maxUnits) {
        int units = 0;
        if (maxUnits <= 0) return 0;
        for (Direction dir : Direction.values()) {
            if (units >= maxUnits) break;
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be == null || be instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity) continue;
            var items = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).resolve().orElse(null);
            if (items == null) continue;
            for (int slot = 0; slot < items.getSlots() && units < maxUnits; slot++) {
                net.minecraft.world.item.ItemStack stack = items.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                int worth = itemWorth(stack, fluids, extras);
                if (worth <= 0) continue;
                int count = stack.getCount();
                while (count > 0 && units + worth <= maxUnits) {
                    if (items.extractItem(slot, 1, false).isEmpty()) break;
                    units += worth;
                    count--;
                }
            }
        }
        return units;
    }

    private static List<? extends String> read(ForgeConfigSpec.ConfigValue<List<? extends String>> value) {
        try {
            return value.get();
        } catch (RuntimeException e) {
            return value.getDefault();
        }
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

    public static boolean hasTank(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (neighbour(level, pos, dir) != null) return true;
        }
        return false;
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
