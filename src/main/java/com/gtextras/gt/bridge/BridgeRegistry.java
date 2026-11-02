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

    private static final String DEFAULT_JSON = "{\n"
            + "  \"einstein_rosen\": {\n"
            + "    \"displayName\": \"Einstein-Rosen Bridge\",\n"
            + "    \"color\": \"6FD3FF\",\n"
            + "    \"returnColor\": \"14141C\",\n"
            + "    \"resonanceCap\": 1000,\n"
            + "    \"resonanceDecayPerSecond\": 5,\n"
            + "    \"range\": 24\n"
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
                    integer(o, "range", 24)));
        }
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
