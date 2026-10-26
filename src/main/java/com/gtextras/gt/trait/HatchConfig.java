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
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Per tier hatch sizes. storage is the internal buffer. rate is the most the hatch moves per tick with neighbouring pools, jars and tanks.");
        for (String resource : RESOURCES) {
            builder.push(resource);
            for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
                builder.push(GTValues.VN[tier].toLowerCase());
                STORAGE.put(key(resource, tier), builder.defineInRange("storage", defaultStorage(resource, tier), 1,
                        Integer.MAX_VALUE));
                RATE.put(key(resource, tier), builder.defineInRange("rate", defaultRate(resource, tier), 1,
                        Integer.MAX_VALUE / 20));
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
            case "mana" -> Math.max(1, storage / 200);
            case "source" -> Math.max(1, storage / 50);
            default -> Math.max(1, storage / 100);
        };
    }

    public static int storage(String resource, int tier) {
        try {
            return STORAGE.get(key(resource, tier)).get();
        } catch (RuntimeException e) {
            return defaultStorage(resource, tier);
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
