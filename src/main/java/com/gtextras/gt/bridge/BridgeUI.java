package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.gtextras.gt.trait.LinkSet;

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

import java.util.function.Supplier;

public final class BridgeUI {

    private static final int WIDTH = 256;
    private static final int INNER = WIDTH - 16;
    private static final int TEXT = 0xFF404040;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ROWS = 12;

    private BridgeUI() {}

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

    private static void button(WidgetGroup group, int x, int y, int width, Supplier<String> shown, Runnable action) {
        group.addWidget(new ButtonWidget(x, y, width, 14, GuiTextures.BUTTON, click -> action.run()));
        LabelWidget label = new LabelWidget(x + 5, y + 3, shown);
        label.setColor(WHITE);
        group.addWidget(label);
    }

    public static ModularUI create(IUIHolder holder, Player player, BridgeContainer container) {
        BridgeDef def = container.def();
        boolean input = container.io() == IO.IN;
        WidgetGroup group = new WidgetGroup(0, 0, WIDTH, 260);
        group.addWidget(new LabelWidget(8, 7, container.owner().getDefinition().getBlock().getName()));
        group.addWidget(text(8, 20, () -> def.displayName + "  -  " + (input ? "receives packets" : "sends packets")));
        int y = 34;
        if (input) {
            int accent = 0xFF000000 | def.color;
            group.addWidget(new ProgressWidget(() -> container.fraction(BridgeContainer.RESONANCE), 8, y, INNER, 12)
                    .setProgressTexture(new ColorRectTexture(0xFF373737), new ColorRectTexture(accent)));
            LabelWidget level = new LabelWidget(12, y + 3, () -> "Resonance  " + String.format("%,d / %,d",
                    container.level(BridgeContainer.RESONANCE), container.resonanceCapacity()));
            level.setColor(WHITE);
            group.addWidget(level);
            y += 18;
        }
        group.addWidget(text(8, y, () -> "Stored packets  (" + container.entryCount() + ")"));
        y += 12;
        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(8, y, INNER, 62);
        list.setYScrollBarWidth(4);
        list.setBackground(new ColorRectTexture(0xFF8B8B8B));
        for (int i = 0; i < ROWS; i++) {
            final int index = i;
            LabelWidget row = new LabelWidget(4, i * 11 + 2, () -> container.entryText(index));
            row.setColor(WHITE);
            list.addWidget(row);
        }
        group.addWidget(list);
        y += 68;
        if (!input) {
            LinkSet links = container.links();
            group.addWidget(text(8, y, () -> "Linked hatches  (" + links.size() + " / " + LinkSet.MAX + ")"));
            y += 12;
            DraggableScrollableWidgetGroup linked = new DraggableScrollableWidgetGroup(8, y, INNER, 44);
            linked.setYScrollBarWidth(4);
            linked.setBackground(new ColorRectTexture(0xFF8B8B8B));
            for (int i = 0; i < LinkSet.MAX; i++) {
                final int index = i;
                LabelWidget row = new LabelWidget(4, i * 11 + 2, () -> describe(container, index));
                row.setColor(WHITE);
                linked.addWidget(row);
                linked.addWidget(new ButtonWidget(INNER - 18, i * 11, 10, 10, click -> {
                    if (container.owner().isRemote()) return;
                    if (index < links.size()) {
                        links.remove(index);
                        container.markLinksDirty();
                    }
                }));
                LabelWidget x = new LabelWidget(INNER - 15, i * 11 + 2, () -> index < links.size() ? "x" : "");
                x.setColor(0xFFFF5555);
                linked.addWidget(x);
            }
            group.addWidget(linked);
            y += 50;
            button(group, 8, y, INNER, () -> "Beam colour: " + (container.returnBeam() ? "return" : "forward"),
                    container::toggleReturnBeam);
            y += 17;
            button(group, 8, y, INNER, () -> "Clear all links", () -> {
                if (container.owner().isRemote()) return;
                links.clear();
                container.markLinksDirty();
            });
            y += 17;
            group.addWidget(text(8, y, "Link with the Link Tool: select this hatch,"));
            y += 10;
            group.addWidget(text(8, y, "then click an input hatch of the same bridge."));
            y += 12;
        }
        boolean[] armed = { false };
        group.addWidget(new ButtonWidget(8, y, INNER, 14, GuiTextures.BUTTON, click -> {
            if (!armed[0]) {
                armed[0] = true;
                return;
            }
            armed[0] = false;
            container.emptyBuffer();
        }));
        LabelWidget empty = new LabelWidget(14, y + 3,
                () -> armed[0] ? "Click again to void every stored packet" : "Empty buffer");
        empty.setColor(WHITE);
        group.addWidget(empty);
        y += 20;
        return new ModularUI(WIDTH, y + 8, holder, player).background(GuiTextures.BACKGROUND).widget(group);
    }

    private static String describe(BridgeContainer container, int index) {
        LinkSet links = container.links();
        if (index >= links.size()) return "";
        BlockPos pos;
        try {
            pos = links.list().get(index);
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
        String name = "Hatch";
        Level level = container.owner().getLevel();
        if (level != null && level.isLoaded(pos)) {
            net.minecraft.resources.ResourceLocation key = ForgeRegistries.BLOCKS
                    .getKey(level.getBlockState(pos).getBlock());
            if (key != null) name = pretty(key.getPath());
        }
        return (index + 1) + ". " + name + " " + pos.getX() + "," + pos.getY() + "," + pos.getZ();
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
