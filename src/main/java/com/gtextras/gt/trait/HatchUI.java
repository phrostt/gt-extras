package com.gtextras.gt.trait;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

public final class HatchUI {

    public static final int WIDTH = 256;
    private static final int INNER = WIDTH - 16;
    private static final int TEXT = 0xFF404040;
    private static final int WHITE = 0xFFFFFFFF;

    private HatchUI() {}

    public static final class Panel {

        public final WidgetGroup group;
        public int y;
        private final AmountContainer container;

        Panel(WidgetGroup group, AmountContainer container) {
            this.group = group;
            this.container = container;
        }

        public AmountContainer container() {
            return container;
        }
    }

    private static LabelWidget text(int x, int y, String value) {
        LabelWidget label = new LabelWidget(x, y, value);
        label.setColor(TEXT);
        return label;
    }

    private static LabelWidget text(int x, int y, Supplier<String> value) {
        LabelWidget label = new LabelWidget(x, y, value);
        label.setColor(TEXT);
        return label;
    }

    public static ModularUI finish(IUIHolder holder, Player player, Panel panel) {
        return new ModularUI(WIDTH, panel.y + 8, holder, player).background(GuiTextures.BACKGROUND)
                .widget(panel.group);
    }

    public static Panel start(AmountContainer container, String unit, int barColor) {
        WidgetGroup group = new WidgetGroup(0, 0, WIDTH, 200);
        Panel panel = new Panel(group, container);
        group.addWidget(new LabelWidget(8, 7, container.owner().getDefinition().getBlock().getName()));
        group.addWidget(new ProgressWidget(() -> container.getCapacity() <= 0 ? 0
                : container.getStored() / (double) container.getCapacity(), 8, 20, INNER, 12)
                .setProgressTexture(new ColorRectTexture(0xFF373737), new ColorRectTexture(barColor)));
        LabelWidget amount = new LabelWidget(12, 23, () -> unit + "  " + String.format("%,d / %,d",
                container.getStored(), container.getCapacity()));
        amount.setColor(WHITE);
        group.addWidget(amount);
        group.addWidget(text(8, 37, () -> String.format("Max transfer: %,d / tick  (%,d / s)",
                container.maxPerTick(), container.maxPerTick() * 20L)));
        group.addWidget(text(8, 49, () -> {
            int cost = container.costPerOperation();
            if (!container.passive()) return "Passive drain: 0 EU/t  (off)";
            if (cost <= 0) return "Passive drain: none";
            return String.format("Passive drain: up to %,.1f EU/t", cost / (double) container.operationInterval());
        }));
        panel.y = 62;
        button(panel, 8, INNER, "Passive", () -> "Passive operation: " + (container.passive() ? "ON" : "OFF"),
                () -> container.setPassive(!container.passive()));
        panel.y += 20;
        emptyButton(panel);
        return panel;
    }

    private static void emptyButton(Panel panel) {
        boolean[] armed = { false };
        AmountContainer container = panel.container;
        panel.group.addWidget(new ButtonWidget(8, panel.y, INNER, 14, GuiTextures.BUTTON, click -> {
            if (!armed[0]) {
                armed[0] = true;
                return;
            }
            armed[0] = false;
            container.emptyBuffer();
        }));
        centered(panel.group, 8, panel.y + 3, () -> armed[0] ? "Click again to void the buffer"
                : "Empty buffer");
        panel.y += 20;
    }

    private static void centered(WidgetGroup group, int x, int y, Supplier<String> text) {
        LabelWidget label = new LabelWidget(x + 6, y, text);
        label.setColor(WHITE);
        group.addWidget(label);
    }

    public static void note(Panel panel, String... lines) {
        for (String line : lines) {
            panel.group.addWidget(text(8, panel.y, line));
            panel.y += 10;
        }
    }

    public static void button(Panel panel, int x, int width, String text, Supplier<String> shown,
                              Runnable action) {
        panel.group.addWidget(new ButtonWidget(x, panel.y, width, 14, GuiTextures.BUTTON, click -> action.run()));
        LabelWidget label = new LabelWidget(x + 5, panel.y + 3, shown == null ? () -> text : shown);
        label.setColor(WHITE);
        panel.group.addWidget(label);
    }

    public static void choices(Panel panel, String heading, List<String> names, Supplier<Integer> selected,
                               IntConsumer pick) {
        panel.group.addWidget(text(8, panel.y, heading));
        panel.y += 12;
        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            final String name = names.get(i);
            button(panel, 8, INNER, name, () -> (selected.get() == index ? "> " : "   ") + name,
                    () -> pick.accept(index));
            panel.y += 15;
        }
    }

    public static void scrollChoices(Panel panel, String heading, int visibleHeight, List<String> names,
                                     Supplier<Integer> selected, IntConsumer pick) {
        panel.group.addWidget(text(8, panel.y, heading));
        panel.y += 12;
        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(8, panel.y, INNER, visibleHeight);
        list.setYScrollBarWidth(4);
        list.setBackground(new ColorRectTexture(0xFF8B8B8B));
        for (int i = 0; i < names.size(); i++) {
            final int index = i;
            final String name = names.get(i);
            list.addWidget(new ButtonWidget(0, i * 13, INNER - 8, 12, GuiTextures.BUTTON, click -> pick.accept(index)));
            LabelWidget label = new LabelWidget(4, i * 13 + 2,
                    () -> (selected.get() == index ? "> " : "   ") + name);
            label.setColor(WHITE);
            list.addWidget(label);
        }
        panel.group.addWidget(list);
        panel.y += visibleHeight + 6;
    }

    public static void links(Panel panel, Linkable linkable, String tool, String heading) {
        LinkSet links = linkable.links();
        panel.group.addWidget(text(8, panel.y, () -> heading + "  (" + links.size() + " / "
                + LinkSet.MAX + ")"));
        panel.y += 12;
        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(8, panel.y, INNER, 50);
        list.setYScrollBarWidth(4);
        list.setBackground(new ColorRectTexture(0xFF8B8B8B));
        for (int i = 0; i < LinkSet.MAX; i++) {
            final int index = i;
            LabelWidget text = new LabelWidget(4, i * 11 + 2, () -> describe(linkable, index));
            text.setColor(WHITE);
            list.addWidget(text);
            list.addWidget(new ButtonWidget(INNER - 18, i * 11, 10, 10, click -> {
                if (index < links.size()) {
                    links.remove(index);
                    linkable.markLinksDirty();
                }
            }));
            LabelWidget x = new LabelWidget(INNER - 15, i * 11 + 2, () -> index < links.size() ? "x" : "");
            x.setColor(0xFFFF5555);
            list.addWidget(x);
        }
        panel.group.addWidget(list);
        panel.y += 56;
        panel.group.addWidget(text(8, panel.y, "Distribution"));
        panel.y += 11;
        button(panel, 8, INNER / 2 - 2, "In order", () -> (links.roundRobin() ? "   " : "> ") + "In order",
                () -> {
                    links.setRoundRobin(false);
                    linkable.markLinksDirty();
                });
        button(panel, 8 + INNER / 2 + 2, INNER / 2 - 2, "Round robin", () -> (links.roundRobin() ? "> " : "   ") + "Round robin",
                () -> {
                    links.setRoundRobin(true);
                    linkable.markLinksDirty();
                });
        panel.y += 17;
        button(panel, 8, INNER, "Clear all links", null, () -> {
            links.clear();
            linkable.markLinksDirty();
        });
        panel.y += 17;
        note(panel, "Link with the " + tool + ".", "Nothing is used unless it is linked.");
    }

    private static String describe(Linkable linkable, int index) {
        LinkSet links = linkable.links();
        if (index >= links.size()) return "";
        BlockPos pos;
        try {
            pos = links.list().get(index);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
        String name = "Target";
        Level level = linkable.linkOrigin() == null ? null : levelOf(linkable);
        if (level != null && level.isLoaded(pos)) {
            net.minecraft.resources.ResourceLocation key = ForgeRegistries.BLOCKS
                    .getKey(level.getBlockState(pos).getBlock());
            if (key != null) name = pretty(key.getPath());
        }
        return (index + 1) + ". " + name + " " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static Level levelOf(Linkable linkable) {
        return linkable instanceof AmountContainer container ? container.owner().getLevel() : null;
    }

    private static String pretty(String path) {
        StringBuilder out = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }
}
