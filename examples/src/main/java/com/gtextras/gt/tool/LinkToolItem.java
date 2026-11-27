package com.gtextras.gt.tool;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;

import com.gtextras.gt.trait.Linkable;
import com.gtextras.gt.trait.TransferFx;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class LinkToolItem extends Item {

    private static final String TAG = "gtextras_hatch";

    public LinkToolItem() {
        super(new Item.Properties().stacksTo(1));
    }

    private static Linkable hatchAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MetaMachineBlockEntity holder)) return null;
        for (var trait : holder.getMetaMachine().getTraits()) {
            if (trait instanceof Linkable linkable && linkable.linkable()) return linkable;
        }
        return null;
    }

    private static void say(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clicked = context.getClickedPos();
        if (player == null) return InteractionResult.PASS;

        Linkable hatch = hatchAt(level, clicked);
        if (hatch != null) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (player.isShiftKeyDown()) {
                for (BlockPos target : hatch.links().list()) {
                    TransferFx.line(level, clicked, target, TransferFx.REMOVED, 0.8f);
                }
                hatch.links().clear();
                hatch.markLinksDirty();
                say(player, "gtextras.tool.cleared");
                return InteractionResult.SUCCESS;
            }
            stack.getOrCreateTag().putLong(TAG, clicked.asLong());
            say(player, "gtextras.tool.selected", hatch.links().size());
            show(level, clicked, hatch, 1.2);
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) {
            if (level.getBlockEntity(clicked) instanceof wayoftime.bloodmagic.altar.IBloodAltar) {
                if (!level.isClientSide) say(player, "gtextras.tool.select_first");
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockPos hatchPos = BlockPos.of(tag.getLong(TAG));
        Linkable selected = hatchAt(level, hatchPos);
        if (selected == null) {
            tag.remove(TAG);
            say(player, "gtextras.tool.lost");
            return InteractionResult.SUCCESS;
        }
        int range = selected.linkRange();
        if (clicked.distSqr(hatchPos) > (double) range * range) {
            say(player, "gtextras.tool.far", range);
            return InteractionResult.SUCCESS;
        }
        if (!selected.canLink(level, clicked)) {
            say(player, "gtextras.tool.invalid");
            return InteractionResult.SUCCESS;
        }
        int result = selected.links().toggle(clicked);
        selected.markLinksDirty();
        if (result > 0) {
            TransferFx.line(level, hatchPos, clicked, selected.linkColor(), 1.0f);
            TransferFx.orb(level, hatchPos, clicked, selected.linkColor());
            say(player, "gtextras.tool.linked", selected.links().size(), com.gtextras.gt.trait.LinkSet.MAX);
        } else if (result == 0) {
            TransferFx.line(level, hatchPos, clicked, TransferFx.REMOVED, 1.0f);
            say(player, "gtextras.tool.unlinked", selected.links().size(), com.gtextras.gt.trait.LinkSet.MAX);
        } else {
            say(player, "gtextras.tool.full", com.gtextras.gt.trait.LinkSet.MAX);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();
        if (player.isShiftKeyDown() && tag != null && tag.contains(TAG)) {
            if (!level.isClientSide) {
                tag.remove(TAG);
                if (tag.isEmpty()) stack.setTag(null);
                say(player, "gtextras.tool.deselected");
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player)) return;
        if (level.getGameTime() % 10 != 0) return;
        if (!selected && player.getOffhandItem() != stack) return;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) return;
        BlockPos hatchPos = BlockPos.of(tag.getLong(TAG));
        if (!level.isLoaded(hatchPos)) return;
        Linkable hatch = hatchAt(level, hatchPos);
        if (hatch == null) return;
        show(level, hatchPos, hatch, 1.2);
    }

    private static void show(Level level, BlockPos hatchPos, Linkable hatch, double density) {
        int[] color = hatch.linkColor();
        for (BlockPos target : hatch.links().list()) {
            TransferFx.line(level, hatchPos, target, color, 0.7f, density);
        }
        TransferFx.line(level, hatchPos, hatchPos.above(), color, 1.2f, 3);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gtextras.tool.tip1").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtextras.tool.tip2").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("gtextras.tool.tip3").withStyle(net.minecraft.ChatFormatting.GRAY));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG)) {
            BlockPos pos = BlockPos.of(tag.getLong(TAG));
            tooltip.add(Component.translatable("gtextras.tool.tip_selected", pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(net.minecraft.ChatFormatting.AQUA));
        }
    }
}
