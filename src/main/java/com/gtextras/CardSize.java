package com.gtextras;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CardSize {

    private static final Map<String, Integer> EXTRA = new ConcurrentHashMap<>();

    private static boolean ANNOUNCED;
    private static final java.util.Set<String> WARNED_NO_LEVEL = ConcurrentHashMap.newKeySet();

    private CardSize() {
    }

    private static String version() {
        try {
            return net.minecraftforge.fml.ModList.get().getModContainerById(GTExtras.MOD_ID)
                    .map(c -> c.getModInfo().getVersion().toString()).orElse("unknown");
        } catch (Throwable t) {
            return "unknown";
        }
    }

    public static int cached(ResourceLocation category) {
        if (category == null) {
            return 0;
        }
        Integer value = EXTRA.get(category.toString());
        return value != null ? value : 0;
    }

    public static Object grow(Object ui, Object size) {
        try {
            int extra = extraFor(ui);
            if (extra <= 0 || size == null) {
                return null;
            }
            int width = size.getClass().getField("width").getInt(size);
            int height = size.getClass().getField("height").getInt(size);
            return size.getClass().getConstructor(int.class, int.class).newInstance(width, height + extra);
        } catch (Throwable t) {
            return null;
        }
    }

    public static int extraFor(Object ui) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (!ANNOUNCED) {
                ANNOUNCED = true;
                GTExtras.LOGGER.info("[{}] card sizing active, version {}", GTExtras.MOD_ID, version());
            }
            if (mc.level == null) {
                if (WARNED_NO_LEVEL.add(String.valueOf(System.identityHashCode(ui)))) {
                    GTExtras.LOGGER.info("[{}] card size requested before a world was loaded, no extra height", GTExtras.MOD_ID);
                }
                return 0;
            }
            RecipeManager manager = mc.level.getRecipeManager();
            GTRecipeType type = typeOf(ui);
            if (type == null) {
                return 0;
            }
            String id = type.registryName.toString();
            Integer known = EXTRA.get(id);
            if (known != null) {
                return known;
            }
            int value = measure(type, manager);
            EXTRA.put(id, value);
            GTExtras.LOGGER.info("[{}] card extra: {} = {}", GTExtras.MOD_ID, id, value);
            return value;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static GTRecipeType typeOf(Object ui) throws ReflectiveOperationException {
        for (Class<?> c = ui.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (GTRecipeType.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return (GTRecipeType) f.get(ui);
                }
            }
        }
        return null;
    }

    private static int measure(GTRecipeType type, RecipeManager manager) {
        CoilConfig config = CoilConfig.get();
        if (!config.enabled) {
            return 0;
        }
        int maxLines = 0;
        int maxBadges = 0;
        boolean icon = false;
        List<ExtraBadges.TextLine> savedText = ExtraBadges.snapshotText();
        List<GTRecipe> recipes = manager.getAllRecipesFor(type);
        try {
        for (GTRecipe recipe : recipes) {
            CoilInfo.Result info = CoilInfo.resolve(config, type.registryName, recipe);
            List<ExtraBadges.Badge> badges = ExtraBadges.of(config, recipe);
            int lines = badges.isEmpty() ? 0 : ExtraBadges.lineCount();
            boolean nativeCoil = info != null && info.hasCoil() && NativeLines.showsCoilText(recipe);
            if (info != null && info.hasCoil() && config.showText && !nativeCoil) {
                lines += 2;
            }
            if (info != null && !nativeCoil) {
                icon = true;
            }
            maxLines = Math.max(maxLines, Math.min(lines, ExtraBadges.MAX_TEXT_LINES));
            maxBadges = Math.max(maxBadges, badges.size());
        }
        } finally {
            ExtraBadges.restoreText(savedText);
        }
        return CoilInfo.extraHeight(maxLines, maxBadges, icon);
    }
}
