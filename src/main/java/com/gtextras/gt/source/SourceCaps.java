package com.gtextras.gt.source;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;

public final class SourceCaps {

    private static final String BIND_TAG = "gtextras_hatch";

    private SourceCaps() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST, SourceCaps::onRightClick);
        if (net.minecraftforge.fml.ModList.get().isLoaded("arseng")) {
            MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, SourceCaps::attach);
        }
    }

    private static final ResourceLocation BUS_ID = new ResourceLocation(com.gtextras.GTExtras.MOD_ID, "source_bus");

    private static void attach(net.minecraftforge.event.AttachCapabilitiesEvent<BlockEntity> event) {
        if (!(event.getObject() instanceof MetaMachineBlockEntity be)) return;
        event.addCapability(BUS_ID, new net.minecraftforge.common.capabilities.ICapabilityProvider() {

            private net.minecraftforge.common.util.LazyOptional<Object> cached;

            @Override
            public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(
                    net.minecraftforge.common.capabilities.Capability<T> cap, net.minecraft.core.Direction side) {
                if (cap != ArsEngLink.capability()) return net.minecraftforge.common.util.LazyOptional.empty();
                if (!(be.getMetaMachine() instanceof SourceHatchMachine hatch)) {
                    return net.minecraftforge.common.util.LazyOptional.empty();
                }
                if (cached == null || !cached.isPresent()) {
                    Object wrapped = ArsEngLink.wrap(hatch.container());
                    if (wrapped == null) return net.minecraftforge.common.util.LazyOptional.empty();
                    cached = net.minecraftforge.common.util.LazyOptional.of(() -> wrapped);
                }
                return cached.cast();
            }
        });
    }

    private static SourceContainer outputOf(BlockEntity be) {
        if (be instanceof MetaMachineBlockEntity holder && holder.getMetaMachine() instanceof SourceHatchMachine hatch) {
            return hatch.container();
        }
        return null;
    }

    private static boolean isWand(ItemStack stack) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && key.getNamespace().equals("arsnouveau") && key.getPath().contains("dominion");
    }

    private static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        ItemStack stack = event.getItemStack();
        if (!isWand(stack)) return;
        BlockPos clicked = event.getPos();
        BlockEntity clickedBe = level.getBlockEntity(clicked);
        CompoundTag tag = stack.getTag();
        boolean hasPick = tag != null && tag.contains(BIND_TAG);

        SourceContainer picked = outputOf(clickedBe);
        boolean linking = hasPick && clickedBe instanceof ISourceTile;
        if (picked == null && !linking) return;

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) return;

        if (picked != null) {
            if (player.isShiftKeyDown()) {
                picked.links().clear();
                picked.markLinksDirty();
                player.displayClientMessage(Component.translatable("gtextras.hatch.source_bind_clear"), true);
                return;
            }
            stack.getOrCreateTag().putLong(BIND_TAG, clicked.asLong());
            player.displayClientMessage(Component.translatable("gtextras.hatch.source_bind_pick"), true);
            return;
        }

        BlockPos hatchPos = BlockPos.of(tag.getLong(BIND_TAG));
        SourceContainer hatch = outputOf(level.getBlockEntity(hatchPos));
        if (hatch == null) {
            tag.remove(BIND_TAG);
            return;
        }
        if (clicked.distSqr(hatchPos) > SourceContainer.RANGE * SourceContainer.RANGE) {
            player.displayClientMessage(Component.translatable("gtextras.hatch.source_bind_far"), true);
            return;
        }
        int result = hatch.links().toggle(clicked);
        hatch.markLinksDirty();
        player.displayClientMessage(Component.translatable(
                result > 0 ? "gtextras.hatch.source_bind_ok" : result == 0 ? "gtextras.hatch.source_bind_removed"
                        : "gtextras.hatch.source_bind_full", hatch.links().size()), true);
    }
}
