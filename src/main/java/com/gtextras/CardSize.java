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

    private CardSize() {
    }

    public static int cached(ResourceLocation category) {
        if (category == null) {
            return CoilInfo.JEI_EXTRA_HEIGHT;
        }
        Integer value = EXTRA.get(category.toString());
        return value != null ? value : CoilInfo.JEI_EXTRA_HEIGHT;
    }

    public static int extraFor(Object ui) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return CoilInfo.JEI_EXTRA_HEIGHT;
            }
            RecipeManager manager = mc.level.getRecipeManager();
            GTRecipeType type = typeOf(ui);
            if (type == null) {
                return CoilInfo.JEI_EXTRA_HEIGHT;
            }
            String id = type.registryName.toString();
            Integer known = EXTRA.get(id);
            if (known != null) {
                return known;
            }
            int value = measure(type, manager);
            EXTRA.put(id, value);
            return value;
        } catch (Throwable t) {
            return CoilInfo.JEI_EXTRA_HEIGHT;
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
        List<GTRecipe> recipes = manager.getAllRecipesFor(type);
        for (GTRecipe recipe : recipes) {
            CoilInfo.Result info = CoilInfo.resolve(config, type.registryName, recipe);
            List<ExtraBadges.Badge> badges = ExtraBadges.of(config, recipe);
            int lines = badges.isEmpty() ? 0 : ExtraBadges.lineCount();
            if (info != null && info.hasCoil() && config.showText) {
                lines += 2;
            }
            if (info != null) {
                icon = true;
            }
            maxLines = Math.max(maxLines, Math.min(lines, ExtraBadges.MAX_TEXT_LINES));
            maxBadges = Math.max(maxBadges, badges.size());
        }
        return CoilInfo.extraHeight(maxLines, maxBadges, icon);
    }
}
