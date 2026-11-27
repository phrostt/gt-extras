package com.gtextras;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.gregtechceu.gtceu.api.GTValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class RecipeRow {

    private static final Map<Object, Boolean> FRAMED = Collections.synchronizedMap(new WeakHashMap<>());

    private RecipeRow() {
    }

    public static void apply(Object widget) {
        try {
            CoilConfig config = CoilConfig.get();
            if (!config.enabled || !(widget instanceof WidgetGroup group)
                    || !(config.jeiEnabled || net.minecraftforge.fml.ModList.get().isLoaded("emi"))) {
                return;
            }
            if (!(field(widget, "recipe") instanceof GTRecipe recipe)) {
                return;
            }
            ResourceLocation category = recipe.recipeType.registryName;
            if (!CardSize.hasRow(category)) {
                return;
            }
            LabelWidget voltage = field(widget, "voltageTextWidget") instanceof LabelWidget label ? label : null;
            Object offset = field(widget, "xOffset");
            int xOffset = offset instanceof Integer value ? value : 0;
            CoilInfo.Layout layout = CoilInfo.layout(config, category, recipe, group.getSize().width, group.getSize().height);

            if (FRAMED.put(widget, Boolean.TRUE) == null) {
                CoilInfo.Result info = CoilInfo.resolve(config, category, recipe);
                boolean ownCoil = info != null && info.hasCoil();
                List<Integer> boxes = new ArrayList<>();
                if (info != null && info.hasResearch()) {
                    boxes.add(layout.dataX());
                }
                if (ownCoil) {
                    boxes.add(layout.iconX());
                }
                if (ExtraBadges.circuitOf(config, recipe) != null) {
                    boxes.add(layout.circuitX());
                }
                if (voltage != null) {
                    boxes.add(layout.voltageX());
                }
                int index = voltage != null ? group.widgets.indexOf(voltage) : group.widgets.size();
                if (index < 0) {
                    index = group.widgets.size();
                }
                for (int x : boxes) {
                    group.addWidget(index++, frame(x, xOffset, layout));
                }
                if (voltage != null) {
                    moveButton(group, voltage, layout, xOffset);
                    int at = group.widgets.indexOf(voltage);
                    group.addWidget(at < 0 ? group.widgets.size() : at,
                            new TierBadge(widget, layout.voltageX() - xOffset, layout.iconY()));
                }
            }

            if (voltage != null) {
                voltage.setVisible(false);
            }
        } catch (Throwable ignored) {
        }
    }

    private static final class TierBadge extends Widget {
        private static final int[] COLORS = {
                0x5C6370, 0x9AA0A6, 0xE8913A, 0xD9B72E, 0x8A55D6, 0x3F6FD8, 0xD35FA6, 0x2FA9BC,
                0x3FAE62, 0xC4443C, 0x9C3FB8, 0x2E8F7A, 0xD2762B, 0x4A5DD0, 0xC8C8D2};
        private final Object owner;

        TierBadge(Object owner, int x, int y) {
            super(x, y, CoilInfo.SLOT_SIZE, CoilInfo.SLOT_SIZE);
            this.owner = owner;
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int tier = 0;
            try {
                if (field(owner, "tier") instanceof Integer value) {
                    tier = Math.max(0, Math.min(GTValues.VN.length - 1, value));
                }
            } catch (Throwable ignored) {
            }
            int base = COLORS[Math.min(tier, COLORS.length - 1)];
            int x = getPositionX() + 1;
            int y = getPositionY() + 1;
            int size = CoilInfo.SLOT_SIZE - 2;
            int top = 0xFF000000 | lighten(base, 0.35f);
            int bottom = 0xFF000000 | darken(base, 0.75f);
            graphics.fillGradient(x, y, x + size, y + size, top, bottom);
            graphics.fill(x, y, x + size, y + 1, 0x55FFFFFF);
            String text = GTValues.VN[tier].replaceAll("\\u00a7.", "");
            var font = Minecraft.getInstance().font;
            int width = Math.max(1, font.width(text));
            float scale = Math.min(1.0f, (size - 2) / (float) width);
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(x + size / 2.0f, y + size / 2.0f, 0);
            pose.scale(scale, scale, 1);
            graphics.drawString(font, text, -width / 2, -4, 0xFFFFFFFF, true);
            pose.popPose();
        }

        private static int lighten(int c, float f) {
            int r = (c >> 16 & 255), g = (c >> 8 & 255), b = (c & 255);
            return ((int) (r + (255 - r) * f) << 16) | ((int) (g + (255 - g) * f) << 8) | (int) (b + (255 - b) * f);
        }

        private static int darken(int c, float f) {
            int r = (c >> 16 & 255), g = (c >> 8 & 255), b = (c & 255);
            return ((int) (r * f) << 16) | ((int) (g * f) << 8) | (int) (b * f);
        }
    }

    private static ImageWidget frame(int x, int xOffset, CoilInfo.Layout layout) {
        return new ImageWidget(x - xOffset, layout.iconY(), CoilInfo.SLOT_SIZE, CoilInfo.SLOT_SIZE, GuiTextures.SLOT);
    }

    private static final ThreadLocal<Integer> START = new ThreadLocal<>();

    public static void beginNative(WidgetGroup group) {
        START.set(group.widgets.size());
    }

    private static boolean isCoilItem(SlotWidget slot) {
        try {
            net.minecraft.world.item.ItemStack stack = slot.getHandler() == null ? null : slot.getHandler().getItem();
            return stack != null && !stack.isEmpty()
                    && net.minecraft.world.level.block.Block.byItem(stack.getItem())
                    instanceof com.gregtechceu.gtceu.common.block.CoilBlock;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isRenderOnly(SlotWidget slot) {
        if (!(slot instanceof com.gregtechceu.gtceu.api.gui.widget.SlotWidget gtSlot)) {
            return false;
        }
        com.lowdragmc.lowdraglib.jei.IngredientIO io = gtSlot.getIngredientIO();
        return io == null || io == com.lowdragmc.lowdraglib.jei.IngredientIO.RENDER_ONLY;
    }

    public static void relocateNative(WidgetGroup group, GTRecipe recipe) {
        Integer start = START.get();
        START.remove();
        try {
            CoilConfig config = CoilConfig.get();
            if (start == null || !config.enabled
                    || !(config.jeiEnabled || net.minecraftforge.fml.ModList.get().isLoaded("emi"))) {
                return;
            }
            ResourceLocation category = recipe.recipeType.registryName;
            if (!CardSize.hasRow(category)) {
                return;
            }
            List<SlotWidget> added = new ArrayList<>();
            List<Widget> tail = new ArrayList<>(group.widgets.subList(Math.min(start, group.widgets.size()), group.widgets.size()));
            for (Widget w : tail) {
                if (w instanceof SlotWidget slot) {
                    added.add(slot);
                }
            }
            SlotWidget coil = null;
            for (SlotWidget slot : added) {
                if (isCoilItem(slot)) {
                    coil = slot;
                    break;
                }
            }
            if (coil == null) {
                for (SlotWidget slot : added) {
                    if (isRenderOnly(slot)) {
                        coil = slot;
                        break;
                    }
                }
            }
            if (coil == null) {
                return;
            }
            CoilInfo.Result info = CoilInfo.resolve(config, category, recipe);
            if (info != null && info.hasCoil()) {
                coil.setVisible(false);
                coil.setActive(false);
                return;
            }
            Object offset = field(group, "xOffset");
            int xOffset = offset instanceof Integer value ? value : 0;
            CoilInfo.Layout layout = CoilInfo.layout(config, category, recipe, group.getSize().width, group.getSize().height);
            int fx = layout.iconX() - xOffset;
            int fy = layout.iconY();
            int at = group.widgets.indexOf(coil);
            coil.setSelfPosition(fx, fy);
            if (at >= 0) {
                group.addWidget(at, new ImageWidget(fx, fy, CoilInfo.SLOT_SIZE, CoilInfo.SLOT_SIZE, GuiTextures.SLOT));
            }
        } catch (Throwable error) {
            com.gtextras.GTExtras.LOGGER.warn("[{}] could not move the native coil slot", com.gtextras.GTExtras.MOD_ID, error);
        }
    }

    private static void moveButton(WidgetGroup group, LabelWidget voltage, CoilInfo.Layout layout, int xOffset) {
        for (Widget child : new ArrayList<>(group.widgets)) {
            if (child instanceof ButtonWidget button
                    && button.getSelfPositionX() == voltage.getSelfPositionX()
                    && button.getSelfPositionY() == voltage.getSelfPositionY()) {
                button.setSelfPosition(layout.voltageX() - xOffset, layout.iconY());
                button.setSize(CoilInfo.SLOT_SIZE, CoilInfo.SLOT_SIZE);
                return;
            }
        }
    }

    private static int textWidth(LabelWidget label) {
        try {
            Object text = field(label, "lastTextValue");
            if (text instanceof String string && !string.isEmpty()) {
                return Math.min(CoilInfo.SLOT_SIZE, Minecraft.getInstance().font.width(string));
            }
        } catch (Throwable ignored) {
        }
        return Math.max(1, Math.min(CoilInfo.SLOT_SIZE, label.getSizeWidth()));
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
