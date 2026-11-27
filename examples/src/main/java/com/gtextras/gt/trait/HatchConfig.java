package com.gtextras.gt.trait;

import com.gregtechceu.gtceu.api.GTValues;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.HashMap;
import java.util.Map;

public final class HatchConfig {

    public static final int MIN_TIER = GTValues.LV;
    public static final int MAX_TIER = GTValues.UV;
    private static final String[] RESOURCES = { "mana", "source", "blood", "will" };

    private static final Map<String, ForgeConfigSpec.IntValue> STORAGE = new HashMap<>();
    private static final Map<String, ForgeConfigSpec.IntValue> RATE = new HashMap<>();
    private static final Map<String, ForgeConfigSpec.IntValue> COST = new HashMap<>();
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue PARTICLES;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Per tier hatch sizes. storage is the internal buffer. rate is the most the hatch moves per tick (an older world config file keeps old values, delete it to pick up new defaults) with neighbouring pools, jars and tanks.");
        PARTICLES = builder.comment("Spawn Ars Nouveau style orbs when a mana or source hatch moves resources to or from a neighbouring block.")
                .define("transferParticles", true);
        for (String resource : RESOURCES) {
            builder.push(resource);
            for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
                builder.push(GTValues.VN[tier].toLowerCase());
                STORAGE.put(key(resource, tier), builder.defineInRange("storage", defaultStorage(resource, tier), 1,
                        Integer.MAX_VALUE));
                RATE.put(key(resource, tier), builder.defineInRange("rate", defaultRate(resource, tier), 1,
                        Integer.MAX_VALUE / 20));
                COST.put(key(resource, tier), builder.comment("EU drawn each time the hatch moves something on its own. 0 means free. Paid from the energy hatches of the multiblock the hatch is part of. With no formed multiblock or no energy there, nothing moves.")
                        .defineInRange("euPerOperation", 0, 0, Integer.MAX_VALUE));
                builder.pop();
            }
            builder.pop();
        }
        SPEC = builder.build();
    }

    private HatchConfig() {}

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC, "gtextras-hatches.toml");
    }

    private static String key(String resource, int tier) {
        return resource + ":" + tier;
    }

    private static int defaultStorage(String resource, int tier) {
        int shift = Math.max(0, tier - 1);
        return switch (resource) {
            case "mana" -> 100_000 << shift;
            case "source" -> 10_000 << shift;
            default -> 20_000 << shift;
        };
    }

    private static int defaultRate(String resource, int tier) {
        int storage = defaultStorage(resource, tier);
        return switch (resource) {
            case "mana" -> Math.max(1, storage / 1000);
            case "source" -> Math.max(1, storage / 250);
            default -> Math.max(1, storage / 100);
        };
    }

    public static boolean particles() {
        try {
            return PARTICLES.get();
        } catch (RuntimeException e) {
            return true;
        }
    }

    public static int storage(String resource, int tier) {
        try {
            return STORAGE.get(key(resource, tier)).get();
        } catch (RuntimeException e) {
            return defaultStorage(resource, tier);
        }
    }

    public static int cost(String resource, int tier) {
        try {
            return COST.get(key(resource, tier)).get();
        } catch (RuntimeException e) {
            return 0;
        }
    }

    public static int rate(String resource, int tier) {
        try {
            return RATE.get(key(resource, tier)).get();
        } catch (RuntimeException e) {
            return defaultRate(resource, tier);
        }
    }
}
