package com.gtextras.gt.will;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.gtextras.gt.ExtrasCapabilities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WillHatchMachine extends TieredPartMachine implements IInteractedMachine {

    private final IO io;
    private final WillContainer[] containers = new WillContainer[ExtrasCapabilities.WILL_NAMES.length];

    public WillHatchMachine(IMachineBlockEntity holder, IO io, int tier) {
        super(holder, tier);
        this.io = io;
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new WillContainer(this, io, tier, i);
        }
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (!world.isClientSide) {
                StringBuilder text = new StringBuilder();
                for (WillContainer container : containers) {
                    if (text.length() > 0) text.append("  ");
                    String name = ExtrasCapabilities.WILL_NAMES[container.typeIndex()];
                    text.append(Character.toUpperCase(name.charAt(0))).append(name, 1, 3).append(' ')
                            .append(String.format("%,d", container.getStored()));
                }
                player.displayClientMessage(Component.translatable("gtextras.hatch.will_status", text.toString()),
                        true);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand,
                                                   net.minecraft.core.Direction gridSide, BlockHitResult hit) {
        if (io == IO.OUT && player.isShiftKeyDown()) {
            if (!isRemote()) {
                boolean gems = !containers[0].gemMode();
                for (WillContainer container : containers) {
                    container.setGemMode(gems);
                }
                player.displayClientMessage(Component.translatable(
                        gems ? "gtextras.hatch.will_mode_gems" : "gtextras.hatch.will_mode_fluid"), true);
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return super.onScrewdriverClick(player, hand, gridSide, hit);
    }
}
