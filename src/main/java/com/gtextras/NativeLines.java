package com.gtextras;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class NativeLines {

    private static final Map<Object, Integer> LAST_LABEL_Y = Collections.synchronizedMap(new WeakHashMap<>());

    private NativeLines() {
    }

    public static boolean showsCoilText(Object recipe) {
        if (!(recipe instanceof GTRecipe gt) || gt.data == null) {
            return false;
        }
        try {
            for (Function<CompoundTag, String> info : gt.recipeType.getDataInfos()) {
                String text = info.apply(gt.data);
                if (text != null && text.toLowerCase(Locale.ROOT).contains("coil")) {
                    return true;
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return false;
    }

    public static int coilLineY(Object recipe, int displayHeight) {
        Integer y = LAST_LABEL_Y.get(recipe);
        return y != null ? y : displayHeight - 30;
    }

    public static void record(Object widget) {
        try {
            Object recipe = field(widget, "recipe");
            Object children = field(widget, "widgets");
            if (recipe == null || !(children instanceof List<?> list)) {
                return;
            }
            int best = Integer.MIN_VALUE;
            for (Object child : list) {
                if (child == null || !child.getClass().getSimpleName().equals("LabelWidget")) {
                    continue;
                }
                Integer y = yOf(child);
                if (y != null && y > best) {
                    best = y;
                }
            }
            if (best != Integer.MIN_VALUE) {
                LAST_LABEL_Y.put(recipe, best);
            }
        } catch (Throwable ignored) {
        }
    }

    private static Integer yOf(Object widget) throws ReflectiveOperationException {
        for (String name : new String[]{"getSelfPosition", "getPosition"}) {
            try {
                Method m = widget.getClass().getMethod(name);
                Object position = m.invoke(widget);
                if (position != null) {
                    Field y = position.getClass().getField("y");
                    return y.getInt(position);
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
