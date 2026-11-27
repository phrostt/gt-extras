package com.gtextras.gt.mana;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.GTExtras;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import vazkii.botania.api.BotaniaForgeCapabilities;

public final class ManaCaps {

    private static final ResourceLocation ID = new ResourceLocation(GTExtras.MOD_ID, "mana_receiver");

    private ManaCaps() {}

    private static final String BIND_TAG = "gtextras_hatch";

    public static void init() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, ManaCaps::attach);
        MinecraftForge.EVENT_BUS.addListener(ManaCaps::onRightClick);
    }

    private static void onRightClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        net.minecraft.world.level.Level level = event.getLevel();
        if (level.isClientSide || event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        net.minecraft.world.item.ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof vazkii.botania.common.item.WandOfTheForestItem)) return;
        net.minecraft.nbt.CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(BIND_TAG)) return;
        net.minecraft.core.BlockPos clicked = event.getPos();
        net.minecraft.core.BlockPos hatchPos = net.minecraft.core.BlockPos.of(tag.getLong(BIND_TAG));
        if (clicked.equals(hatchPos)) return;
        BlockEntity target = level.getBlockEntity(clicked);
        BlockEntity hatchBe = level.getBlockEntity(hatchPos);
        tag.remove(BIND_TAG);
        if (stack.getTag() != null && stack.getTag().isEmpty()) stack.setTag(null);
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
        if (!(hatchBe instanceof MetaMachineBlockEntity holder) || !(holder.getMetaMachine() instanceof ManaHatchMachine hatch)) {
            return;
        }
        vazkii.botania.api.mana.ManaReceiver receiver = target == null ? null
                : target.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER).resolve().orElse(null);
        if (receiver == null) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gtextras.hatch.mana_bind_fail"), true);
            return;
        }
        if (clicked.distSqr(hatchPos) > ManaContainer.BIND_RANGE * ManaContainer.BIND_RANGE) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gtextras.hatch.mana_bind_far"), true);
            return;
        }
        int result = hatch.container().links().toggle(clicked);
        hatch.container().markLinksDirty();
        player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                result > 0 ? "gtextras.hatch.mana_bind_ok" : result == 0 ? "gtextras.hatch.mana_bind_removed"
                        : "gtextras.hatch.mana_bind_full", hatch.container().links().size()), true);
    }

    private static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof MetaMachineBlockEntity be) {
            event.addCapability(ID, new Provider(be));
        }
    }

    private static final class Provider implements ICapabilityProvider {

        private final MetaMachineBlockEntity be;
        private LazyOptional<vazkii.botania.api.mana.ManaReceiver> busCached;

        private Provider(MetaMachineBlockEntity be) {
            this.be = be;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == BotaniaForgeCapabilities.WANDABLE) {
                MetaMachine machine = be.getMetaMachine();
                if (machine instanceof ManaHatchMachine hatch) {
                    vazkii.botania.api.block.Wandable wandable = (player, stack, face) -> {
                        if (player == null || player.level().isClientSide) return true;
                        if (player.isShiftKeyDown()) {
                            hatch.container().links().clear();
                            hatch.container().markLinksDirty();
                            player.displayClientMessage(net.minecraft.network.chat.Component
                                    .translatable("gtextras.hatch.mana_bind_clear"), true);
                            return true;
                        }
                        stack.getOrCreateTag().putLong(BIND_TAG, be.getBlockPos().asLong());
                        player.displayClientMessage(net.minecraft.network.chat.Component
                                .translatable("gtextras.hatch.mana_bind_pick"), true);
                        return true;
                    };
                    return LazyOptional.of(() -> wandable).cast();
                }
                return LazyOptional.empty();
            }
            if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
                MetaMachine machine = be.getMetaMachine();
                if (machine instanceof ManaHatchMachine hatch) {
                    if (net.minecraftforge.fml.ModList.get().isLoaded("appbot")) {
                        if (busCached == null || !busCached.isPresent()) {
                            vazkii.botania.api.mana.ManaReceiver wrapped = AppbotLink.wrap(hatch.container());
                            busCached = LazyOptional.of(() -> wrapped);
                        }
                        return busCached.cast();
                    }
                    return LazyOptional.of(hatch::container).cast();
                }
            }
            return LazyOptional.empty();
        }
    }
}
