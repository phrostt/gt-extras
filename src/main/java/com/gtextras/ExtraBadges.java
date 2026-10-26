package com.gtextras;

import com.gtextras.gt.NeedsReader;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public final class ExtraBadges {

    public static final int STEP = 20;

    public record Badge(ItemStack icon, List<Component> tooltip) {}

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
                add(badges, "botania:mana_pool", "mana", needs.manaIn(), needs.manaInTick(), needs.manaOut(),
                        needs.manaOutTick());
            }
            if (needs.hasSource()) {
                add(badges, "ars_nouveau:source_gem", "source", needs.sourceIn(), needs.sourceInTick(),
                        needs.sourceOut(), needs.sourceOutTick());
            }
            if (needs.hasBlood()) {
                add(badges, "bloodmagic:bloodaltar", "blood", needs.bloodIn(), needs.bloodInTick(), needs.bloodOut(),
                        needs.bloodOutTick());
            }
            if (needs.hasRpm()) {
                ItemStack icon = stack("create:speedometer");
                if (!icon.isEmpty()) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(Component.translatable("gtextras.tooltip.badge.rpm").withStyle(ChatFormatting.GOLD));
                    lines.add(Component.translatable("gtextras.tooltip.badge.rpm_req", needs.rpm())
                            .withStyle(ChatFormatting.GRAY));
                    badges.add(new Badge(icon, lines));
                }
            }
            return badges;
        } catch (Throwable t) {
            return List.of();
        }
    }

    public static int firstX(CoilInfo.Layout layout, CoilInfo.Result info) {
        int leftmost;
        if (info == null) {
            leftmost = layout.iconX() + STEP;
        } else if (info.hasTarget()) {
            leftmost = layout.dataX();
        } else {
            leftmost = layout.iconX();
        }
        return leftmost - STEP;
    }

    private static void add(List<Badge> badges, String itemId, String kind, long in, long inTick, long out,
                            long outTick) {
        ItemStack icon = stack(itemId);
        if (icon.isEmpty()) {
            return;
        }
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("gtextras.tooltip.badge." + kind).withStyle(ChatFormatting.GOLD));
        line(lines, "consumes", in);
        line(lines, "consumes_tick", inTick);
        line(lines, "produces", out);
        line(lines, "produces_tick", outTick);
        badges.add(new Badge(icon, lines));
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
