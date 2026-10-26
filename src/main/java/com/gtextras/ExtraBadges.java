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

    public static final int COLUMNS = 6;
    public static final int MAX_CELLS = 6;
    public static final int MAX_TEXT_LINES = 16;
    public static final int LINE_H = 9;
    public static final int TEXT_TOP = 2;
    private static final int TOTAL_IN_COLOR = 0xFFC09090;
    private static final int TOTAL_OUT_COLOR = 0xFF90C090;
    private static final int RPM_COLOR = 0xFFFFD27F;
    public static final int CELL_W = 20;
    public static final int CELL_H = 18;
    private static final int IN_COLOR = 0xFFFF8080;
    private static final int OUT_COLOR = 0xFF80FF80;
    private static final int NEUTRAL_COLOR = 0xFFFFFFFF;

    public record Badge(ItemStack icon, List<Component> tooltip, String text, int color) {}

    public record TextLine(String text, int color) {}

    private static List<TextLine> currentText = List.of();

    public static int lineCount() {
        return Math.min(currentText.size(), MAX_TEXT_LINES);
    }

    public static List<TextLine> snapshotText() {
        return currentText;
    }

    public static void drawText(net.minecraft.client.gui.GuiGraphics g, CoilInfo.Layout layout) {
        drawText(g, layout, currentText);
    }

    public static void drawText(net.minecraft.client.gui.GuiGraphics g, CoilInfo.Layout layout, List<TextLine> all) {
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        int shown = Math.min(all.size(), MAX_TEXT_LINES);
        for (int i = 0; i < shown; i++) {
            String text = all.get(i).text();
            int color = all.get(i).color();
            if (i == shown - 1 && all.size() > MAX_TEXT_LINES) {
                text = "+" + (all.size() - shown + 1) + " more, hover the icons";
                color = 0xFF9A9A9A;
            }
            g.drawString(font, text, layout.gridX(), layout.gridY() + TEXT_TOP + i * LINE_H, color, true);
        }
    }

    private static void resourceLine(List<TextLine> out, String verb, String name, long amount, boolean perTick,
                                     int duration, int color, int totalColor) {
        if (amount <= 0) return;
        String head = verb + " " + String.format("%,d", amount) + " " + name + (perTick ? "/t" : "");
        if (perTick && duration > 0) {
            String total = String.format("%,d", amount * duration) + " total";
            if (head.length() + total.length() + 3 <= 34) {
                out.add(new TextLine(head + " (" + total + ")", color));
            } else {
                out.add(new TextLine(head, color));
                out.add(new TextLine("  " + total, totalColor));
            }
        } else {
            out.add(new TextLine(head, color));
        }
    }

    private static void resourceText(List<TextLine> out, String name, long in, long inTick, long o, long oTick,
                                     int duration) {
        resourceLine(out, "Uses", name, in, false, duration, IN_COLOR, TOTAL_IN_COLOR);
        resourceLine(out, "Uses", name, inTick, true, duration, IN_COLOR, TOTAL_IN_COLOR);
        resourceLine(out, "Makes", name, o, false, duration, OUT_COLOR, TOTAL_OUT_COLOR);
        resourceLine(out, "Makes", name, oTick, true, duration, OUT_COLOR, TOTAL_OUT_COLOR);
    }

    private static List<TextLine> buildText(NeedsReader.Needs needs, int duration) {
        List<TextLine> out = new ArrayList<>();
        resourceText(out, "Mana", needs.manaIn(), needs.manaInTick(), needs.manaOut(), needs.manaOutTick(), duration);
        resourceText(out, "Source", needs.sourceIn(), needs.sourceInTick(), needs.sourceOut(), needs.sourceOutTick(),
                duration);
        resourceText(out, "Blood", needs.bloodIn(), needs.bloodInTick(), needs.bloodOut(), needs.bloodOutTick(),
                duration);
        for (int i = 0; i < needs.will().length; i++) {
            if (needs.hasWill(i)) {
                long[] w = needs.will()[i];
                String name = ExtrasCapabilities.WILL_NAMES[i];
                resourceText(out, Character.toUpperCase(name.charAt(0)) + name.substring(1) + " Will", w[0], w[1],
                        w[2], w[3], duration);
            }
        }
        if (needs.hasRpm()) {
            out.add(new TextLine("Consumes rotation: needs " + needs.rpm() + "+ RPM", RPM_COLOR));
            out.add(new TextLine("Needs an INPUT Rotation Interface", RPM_COLOR));
        }
        if (needs.hasOutRpm()) {
            out.add(new TextLine("Generates rotation: " + Math.min(256, needs.outRpm()) + " RPM", RPM_COLOR));
            out.add(new TextLine("Needs an OUTPUT Rotation Interface", RPM_COLOR));
        }
        return out;
    }

    public static boolean isInput(Badge badge) {
        return badge.color() == IN_COLOR;
    }

    public static int cellX(CoilInfo.Layout layout, int index) {
        return layout.gridX() + (index % COLUMNS) * CELL_W;
    }

    public static int cellY(CoilInfo.Layout layout, int index) {
        return layout.gridY() + TEXT_TOP + lineCount() * LINE_H + 3 + (index / COLUMNS) * CELL_H;
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
        if (badge != null) {
            return;
        }
        net.minecraft.client.gui.Font font = net.minecraft.client.Minecraft.getInstance().font;
        int width = font.width(badge.text());
        g.pose().pushPose();
        g.pose().translate(0, 0, 300);
        g.drawString(font, badge.text(), x + 19, y + 5, badge.color(), true);
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

    private static int currentDuration;

    private ExtraBadges() {}

    public static List<Badge> of(CoilConfig config, Object recipe) {
        if (!config.enabled || !config.showExtraBadges || recipe == null || !ModList.get().isLoaded("gtceu")) {
            return List.of();
        }
        try {
            NeedsReader.Needs needs = NeedsReader.read(recipe);
            currentDuration = recipe instanceof com.gregtechceu.gtceu.api.recipe.GTRecipe gt ? gt.duration : 0;
            currentText = needs == null ? List.of() : buildText(needs, currentDuration);
            if (needs != null && recipe instanceof com.gregtechceu.gtceu.api.recipe.GTRecipe gtRecipe
                    && (needs.hasRpm() || needs.hasOutRpm() || config.stressRecipeTypes.contains(String.valueOf(gtRecipe.recipeType.registryName)))) {
                long load = Math.max(1L, Math.abs(com.gregtechceu.gtceu.api.recipe.RecipeHelper.getRealEUt(gtRecipe).getTotalEU()));
                List<TextLine> withLoad = new ArrayList<>(currentText);
                if (needs.hasOutRpm()) {
                    withLoad.add(new TextLine("Provides " + String.format("%,d", load * 64) + " su total", OUT_COLOR));
                } else {
                    withLoad.add(new TextLine("Rotation load: " + String.format("%,d", load) + " su per RPM",
                            RPM_COLOR));
                    withLoad.add(new TextLine("  " + String.format("%,d", load * 64) + " su at 64 RPM",
                            TOTAL_IN_COLOR));
                }
                currentText = withLoad;
            }
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
            if (needs.hasOutRpm()) {
                ItemStack icon = stack("create:speedometer");
                if (!icon.isEmpty()) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("gtextras.tooltip.badge.rpm_out").withStyle(ChatFormatting.GOLD));
                    lines.add(Component.translatable("gtextras.tooltip.badge.rpm_out_info",
                            Math.min(256, needs.outRpm())).withStyle(ChatFormatting.GRAY));
                    badges.add(new Badge(icon, lines, Integer.toString(Math.min(256, needs.outRpm())),
                            OUT_COLOR));
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
        total(lines, "total_consumes", inTick);
        line(lines, "produces", out);
        line(lines, "produces_tick", outTick);
        total(lines, "total_produces", outTick);
        if (in > 0 || inTick > 0) {
            badges.add(new Badge(icon, lines, "", IN_COLOR));
        } else if (out > 0 || outTick > 0) {
            badges.add(new Badge(icon, lines, "", OUT_COLOR));
        }
    }

    private static void addCell(List<Badge> badges, ItemStack icon, List<Component> lines, long amount,
                                boolean perTick, int color) {
        if (amount > 0) {
            badges.add(new Badge(icon, lines, compact(amount, perTick), color));
        }
    }

    private static void total(List<Component> lines, String key, long perTick) {
        if (perTick > 0 && currentDuration > 0) {
            lines.add(Component.translatable("gtextras.tooltip.badge." + key,
                    String.format("%,d", perTick * currentDuration), String.format("%,d", currentDuration),
                    String.format("%.1f", currentDuration / 20.0)).withStyle(ChatFormatting.DARK_GRAY));
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
