package com.gtextras;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gtextras.gt.stage.StageData;
import com.gtextras.gt.stage.StageRuntime;
import net.minecraft.nbt.ListTag;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class StageDisplay {

    private StageDisplay() {
    }

    public static int linesFor(GTRecipe recipe) {
        return recipe != null && StageData.has(recipe.data) ? StageData.list(recipe.data).size() : 0;
    }

    public static void add(Object widget) {
        try {
            Object recipeObj = field(widget, "recipe");
            if (!(recipeObj instanceof GTRecipe recipe) || !StageData.has(recipe.data)) {
                return;
            }
            Object group = widget;
            Object children = field(widget, "widgets");
            if (!(children instanceof List<?> list)) {
                return;
            }
            int xOffset = (Integer) field(widget, "xOffset");
            int textX = 3 - xOffset;
            int best = 0;
            for (Object child : list) {
                if (child == null || !child.getClass().getSimpleName().equals("LabelWidget")) {
                    continue;
                }
                int[] pos = posOf(child);
                if (pos != null && pos[0] == textX) {
                    best = Math.max(best, pos[1]);
                }
            }
            ListTag stages = StageData.list(recipe.data);
            Class<?> label = Class.forName("com.lowdragmc.lowdraglib.gui.widget.LabelWidget");
            Method addWidget = null;
            for (Class<?> c = group.getClass(); c != null && addWidget == null; c = c.getSuperclass()) {
                for (Method m : c.getDeclaredMethods()) {
                    if (m.getName().equals("addWidget") && m.getParameterCount() == 1) {
                        addWidget = m;
                        break;
                    }
                }
            }
            if (addWidget == null) {
                return;
            }
            addWidget.setAccessible(true);
            int y = best + 10;
            for (int i = 0; i < stages.size(); i++) {
                Object w = label.getConstructor(int.class, int.class, String.class)
                        .newInstance(textX, y, StageRuntime.line(stages.getCompound(i), i));
                addWidget.invoke(group, w);
                y += 10;
            }
        } catch (Throwable t) {
            GTExtras.LOGGER.warn("[{}] stage lines failed: {}", GTExtras.MOD_ID, t.toString());
        }
    }

    private static int[] posOf(Object widget) throws ReflectiveOperationException {
        for (String name : new String[]{"getSelfPosition", "getPosition"}) {
            try {
                Object p = widget.getClass().getMethod(name).invoke(widget);
                if (p != null) {
                    return new int[]{p.getClass().getField("x").getInt(p), p.getClass().getField("y").getInt(p)};
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private static Object field(Object target, String name) throws ReflectiveOperationException {
        for (Class<?> c = target.getClass(); c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }
}
