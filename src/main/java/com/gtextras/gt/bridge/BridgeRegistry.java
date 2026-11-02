package com.gtextras.gt.bridge;

import com.gtextras.GTExtras;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class BridgeRegistry {

    private static final Map<String, BridgeDef> DEFS = new LinkedHashMap<>();
    private static boolean loaded;

    private static final String DEFAULT_JSON = ""
            + "{\n"
            + "  \"einstein_rosen\": {\n"
            + "    \"displayName\": \"Einstein-Rosen Bridge\",\n"
            + "    \"color\": \"000000\",\n"
            + "    \"returnColor\": \"FFFFFF\",\n"
            + "    \"resonanceCap\": 10000,\n"
            + "    \"resonanceDecayPerSecond\": 1,\n"
            + "    \"range\": 24,\n"
            + "    \"tiers\": [\"uv\"],\n"
            + "    \"hatchItem\": \"#gtceu:circuits/uxv\",\n"
            + "    \"energy\": { \"enabled\": false }\n"
            + "  },\n"
            + "  \"wireless_energy\": {\n"
            + "    \"displayName\": \"Wireless Energy\",\n"
            + "    \"color\": \"FFC94A\",\n"
            + "    \"returnColor\": \"3A2A08\",\n"
            + "    \"range\": 64,\n"
            + "    \"tiers\": [\"uv\"],\n"
            + "    \"hatchItem\": \"#gtceu:circuits/opv\",\n"
            + "    \"energy\": {\n"
            + "      \"enabled\": true,\n"
            + "      \"range\": 64,\n"
            + "      \"lossPercent\": 0,\n"
            + "      \"maxAmperage\": 0,\n"
            + "      \"bufferTicks\": 20,\n"
            + "      \"bufferSize\": 0,\n"
            + "      \"allowTierMismatch\": false,\n"
            + "      \"amperages\": [1, 2, 4, 8, 16]\n"
            + "    }\n"
            + "  }\n"
            + "}\n";

    private BridgeRegistry() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;
        Path kube = FMLPaths.GAMEDIR.get().resolve("kubejs").resolve("config").resolve("gtextras_bridges.json");
        Path fallback = FMLPaths.CONFIGDIR.get().resolve("gtextras-bridges.json");
        Path source = Files.exists(kube) ? kube : fallback;
        try {
            if (!Files.exists(source)) {
                Files.createDirectories(source.getParent());
                Files.writeString(source, DEFAULT_JSON, StandardCharsets.UTF_8);
            }
            try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
                parse(JsonParser.parseReader(reader));
            }
        } catch (IOException | RuntimeException e) {
            GTExtras.LOGGER.error("[{}] could not read bridge definitions from {}: {}", GTExtras.MOD_ID, source,
                    e.toString());
        }
        if (DEFS.isEmpty()) {
            parse(JsonParser.parseString(DEFAULT_JSON));
        }
        GTExtras.LOGGER.info("[{}] bridges: {}", GTExtras.MOD_ID, DEFS.keySet());
    }

    private static void parse(JsonElement root) {
        if (!root.isJsonObject()) return;
        for (Map.Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
            if (!entry.getValue().isJsonObject()) continue;
            String id = entry.getKey().toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9_]", "_");
            JsonObject o = entry.getValue().getAsJsonObject();
            int color = color(o, "color", 0x6FD3FF);
            DEFS.put(id, new BridgeDef(id, string(o, "displayName", pretty(id)), color, color(o, "returnColor", color),
                    integer(o, "resonanceCap", 1000), integer(o, "resonanceDecayPerSecond", 5),
                    integer(o, "range", 24), energyEnabled(o), energy(o, "range", 64), energy(o, "lossPercent", 0),
                    energy(o, "maxAmperage", 0), energy(o, "bufferTicks", 20), tiers(id, o),
                    string(o, "hatchItem", "minecraft:ender_eye"), energyLong(o, "bufferSize", 0L), energyAmps(o), energyFlag(o, "allowTierMismatch")));
        }
    }

    private static int[] tiers(String id, JsonObject o) {
        java.util.TreeSet<Integer> found = new java.util.TreeSet<>();
        if (o.has("tiers") && o.get("tiers").isJsonArray()) {
            for (JsonElement e : o.getAsJsonArray("tiers")) {
                String name = e.isJsonPrimitive() ? e.getAsString().trim().toLowerCase(java.util.Locale.ROOT) : "";
                int index = -1;
                for (int t = com.gregtechceu.gtceu.api.GTValues.LV; t <= com.gregtechceu.gtceu.api.GTValues.UV; t++) {
                    if (com.gregtechceu.gtceu.api.GTValues.VN[t].toLowerCase(java.util.Locale.ROOT).equals(name)) {
                        index = t;
                    }
                }
                if (index < 0) {
                    GTExtras.LOGGER.warn("[{}] bridge '{}' lists unknown tier '{}' (use lv, mv, hv, ev, iv, luv, zpm or uv)",
                            GTExtras.MOD_ID, id, name);
                } else {
                    found.add(index);
                }
            }
        }
        if (found.isEmpty()) {
            for (int t = com.gregtechceu.gtceu.api.GTValues.LV; t <= com.gregtechceu.gtceu.api.GTValues.UV; t++) {
                found.add(t);
            }
        }
        return found.stream().mapToInt(Integer::intValue).toArray();
    }

    private static boolean energyEnabled(JsonObject o) {
        try {
            if (!o.has("energy") || !o.get("energy").isJsonObject()) return true;
            JsonObject e = o.getAsJsonObject("energy");
            return !e.has("enabled") || e.get("enabled").getAsBoolean();
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private static int energy(JsonObject o, String key, int fallback) {
        if (!o.has("energy") || !o.get("energy").isJsonObject()) return fallback;
        return integer(o.getAsJsonObject("energy"), key, fallback);
    }

    private static boolean energyFlag(JsonObject o, String key) {
        try {
            if (!o.has("energy") || !o.get("energy").isJsonObject()) return false;
            JsonObject e = o.getAsJsonObject("energy");
            return e.has(key) && e.get(key).getAsBoolean();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static long energyLong(JsonObject o, String key, long fallback) {
        try {
            if (!o.has("energy") || !o.get("energy").isJsonObject()) return fallback;
            JsonObject e = o.getAsJsonObject("energy");
            return e.has(key) ? e.get(key).getAsLong() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    private static int[] energyAmps(JsonObject o) {
        java.util.TreeSet<Integer> found = new java.util.TreeSet<>();
        try {
            if (o.has("energy") && o.get("energy").isJsonObject()) {
                JsonObject e = o.getAsJsonObject("energy");
                if (e.has("amperages") && e.get("amperages").isJsonArray()) {
                    for (JsonElement el : e.getAsJsonArray("amperages")) {
                        int a = el.getAsInt();
                        if (a == 1 || a == 2 || a == 4 || a == 8 || a == 16) {
                            found.add(a);
                        } else {
                            GTExtras.LOGGER.warn("[{}] energy amperage {} is not one of 1, 2, 4, 8, 16, skipped",
                                    GTExtras.MOD_ID, a);
                        }
                    }
                }
            }
        } catch (RuntimeException ex) {
            found.clear();
        }
        return found.stream().mapToInt(Integer::intValue).toArray();
    }

    private static String string(JsonObject o, String key, String fallback) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsString() : fallback;
    }

    private static int integer(JsonObject o, String key, int fallback) {
        try {
            return o.has(key) ? o.get(key).getAsInt() : fallback;
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static int color(JsonObject o, String key, int fallback) {
        if (!o.has(key)) return fallback;
        try {
            JsonElement e = o.get(key);
            String text = e.getAsString().trim();
            if (text.startsWith("#")) text = text.substring(1);
            if (text.startsWith("0x") || text.startsWith("0X")) text = text.substring(2);
            return (int) (Long.parseLong(text, 16) & 0xFFFFFF);
        } catch (RuntimeException e) {
            return fallback;
        }
    }

    private static String pretty(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    public static Collection<BridgeDef> all() {
        return Collections.unmodifiableCollection(DEFS.values());
    }

    public static BridgeDef get(String id) {
        return DEFS.get(id);
    }
}
