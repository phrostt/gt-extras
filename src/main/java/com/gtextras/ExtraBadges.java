package com.gtextras;

import com.gtextras.gt.NeedsReader;

import com.gtextras.gt.ExtrasCapabilities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public final class ExtraBadges {

    public static final int COLUMNS = 3;
    public static final int MAX_CELLS = 6;
    public static final int CELL_W = 26;
    public static final int CELL_H = 20;
    private static final int IN_COLOR = 0xFFFF8080;
    private static final int OUT_COLOR = 0xFF80FF80;
    private static final int NEUTRAL_COLOR = 0xFFFFFFFF;

    public record Badge(ItemStack icon, List<Component> tooltip, String text, int color) {}

    public static int cellX(CoilInfo.Layout layout, int index) {
        return layout.gridX() + (index % COLUMNS) * CELL_W;
    }

    public static int cellY(CoilInfo.Layout layout, int index) {
        return layout.gridY() + (index / COLUMNS) * CELL_H;
    }

    public static String compact(long amount, boolean perTick) {
        String text;
        if (amount >= 1_000_000L) {
            text = (amount / 1_000_000L) + "M";
        } else if (amount >= 1_000L) {
            text = (amount / 1_000L) + "K";
        } else {
            text = Long.toString(amount);
        }
        return perTick ? text + "/t" : text;
    }

    public static void drawAmount(net.minecraft.client.gui.GuiGraphics g, Badge badge, int x, int y) {
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        int width = font.width(badge.text());
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.drawString(font, badge.text(), x + 18 - width, y + 10, badge.color(), true);
        g.pose().popPose();
    }

    private static ItemStack fluidIcon(java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> entries, String fallback) {
        try {
            for (var entry : entries) {
                net.minecraft.world.item.Item bucket = entry.fluid().getBucket();
                if (bucket != net.minecraft.world.item.Items.AIR) {
                    return new ItemStack(bucket);
                }
            }
        } catch (Throwable ignored) {
        }
        return stack(fallback);
    }

    private ExtraBadges() {}

    public static List<Badge> of(CoilConfig config, Object recipe) {
        if (!config.enabled || !config.showExtraBadges || recipe == null || !ModList.get().isLoaded("gtceu")) {
            return List.of();
        }
        try {
            NeedsReader.Needs needs = NeedsReader.read(recipe);
            if (needs == null) {
                return List.of();
            }
            List<Badge> badges = new ArrayList<>(3);
            if (needs.hasMana()) {
                add(badges, fluidIcon(com.gtextras.gt.trait.ExtrasFluids.mana(), "botania:mana_pool"), "mana", needs.manaIn(), needs.manaInTick(), needs.manaOut(),
                        needs.manaOutTick());
            }
            if (needs.hasSource()) {
                add(badges, fluidIcon(com.gtextras.gt.trait.ExtrasFluids.source(), "ars_nouveau:source_gem"), "source", needs.sourceIn(), needs.sourceInTick(),
                        needs.sourceOut(), needs.sourceOutTick());
            }
            if (needs.hasBlood()) {
                add(badges, fluidIcon(com.gtextras.gt.trait.ExtrasFluids.blood(), "bloodmagic:bloodaltar"), "blood", needs.bloodIn(), needs.bloodInTick(), needs.bloodOut(),
                        needs.bloodOutTick());
            }
            for (int i = 0; i < needs.will().length; i++) {
                if (needs.hasWill(i)) {
                    long[] w = needs.will()[i];
                    add(badges, willIcon(i), "will", w[0], w[1], w[2], w[3], ExtrasCapabilities.WILL_NAMES[i]);
                }
            }
            if (needs.hasRpm()) {
                ItemStack icon = stack("create:speedometer");
                if (!icon.isEmpty()) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("gtextras.tooltip.badge.rpm").withStyle(ChatFormatting.GOLD));
                    lines.add(Component.translatable("gtextras.tooltip.badge.rpm_req", needs.rpm())
                            .withStyle(ChatFormatting.GRAY));
                    badges.add(new Badge(icon, lines, Integer.toString(needs.rpm()), NEUTRAL_COLOR));
                }
            }
            badges.sort(java.util.Comparator.comparingInt(b -> b.color() == NEUTRAL_COLOR ? 0 : 1));
            return badges.size() > MAX_CELLS ? new ArrayList<>(badges.subList(0, MAX_CELLS)) : badges;
        } catch (Throwable t) {
            return List.of();
        }
    }

    private static ItemStack willIcon(int index) {
        ItemStack icon = fluidIcon(com.gtextras.gt.trait.ExtrasFluids.will(index), "bloodmagic:soulgemlesser");
        return icon;
    }

    private static void add(List<Badge> badges, ItemStack icon, String kind, long in, long inTick, long out,
                            long outTick, String variant) {
        addInternal(badges, icon, kind, in, inTick, out, outTick, variant);
    }

    private static void add(List<Badge> badges, ItemStack icon, String kind, long in, long inTick, long out,
                            long outTick) {
        addInternal(badges, icon, kind, in, inTick, out, outTick, null);
    }

    private static void addInternal(List<Badge> badges, ItemStack icon, String kind, long in, long inTick, long out,
                            long outTick, String variant) {
        if (icon.isEmpty()) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(variant == null
                ? Component.translatable("gtextras.tooltip.badge." + kind).withStyle(ChatFormatting.GOLD)
                : Component.literal(Character.toUpperCase(variant.charAt(0)) + variant.substring(1) + " ")
                        .append(Component.translatable("gtextras.tooltip.badge." + kind))
                        .withStyle(ChatFormatting.GOLD));
        line(lines, "consumes", in);
        line(lines, "consumes_tick", inTick);
        line(lines, "produces", out);
        line(lines, "produces_tick", outTick);
        addCell(badges, icon, lines, in, false, IN_COLOR);
        addCell(badges, icon, lines, inTick, true, IN_COLOR);
        addCell(badges, icon, lines, out, false, OUT_COLOR);
        addCell(badges, icon, lines, outTick, true, OUT_COLOR);
    }

    private static void addCell(List<Badge> badges, ItemStack icon, List<Component> lines, long amount,
                                boolean perTick, int color) {
        if (amount > 0) {
            badges.add(new Badge(icon, lines, compact(amount, perTick), color));
        }
    }

    private static void line(List<Component> lines, String key, long amount) {
        if (amount > 0) {
            lines.add(Component.translatable("gtextras.tooltip.badge." + key, String.format("%,d", amount))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static ItemStack stack(String id) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null || !BuiltInRegistries.ITEM.containsKey(rl)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(BuiltInRegistries.ITEM.get(rl));
    }
}
