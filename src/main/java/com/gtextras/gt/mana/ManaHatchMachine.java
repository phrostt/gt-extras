package com.gtextras.gt.mana;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
<<<<<<< HEAD
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import com.gtextras.gt.trait.HatchUI;

=======
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

<<<<<<< HEAD
public class ManaHatchMachine extends TieredPartMachine implements IUIMachine {
=======
public class ManaHatchMachine extends TieredPartMachine implements IInteractedMachine {
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

    private final ManaContainer container;

    public ManaHatchMachine(IMachineBlockEntity holder, IO io, int tier) {
        super(holder, tier);
        this.container = new ManaContainer(this, io, tier);
    }

    public ManaContainer container() {
        return container;
    }

    @Override
<<<<<<< HEAD
    public ModularUI createUI(Player player) {
        HatchUI.Panel panel = HatchUI.start(container, "Mana", 0xFF46AAFF);
        HatchUI.links(panel, container, "Link Tool or Wand of the Forest", container.io() == IO.OUT ? "Linked targets" : "Linked sources");
        return HatchUI.finish(this, player, panel);
=======
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!player.isShiftKeyDown() && player.getItemInHand(hand).isEmpty()) {
            if (!world.isClientSide) {
                player.displayClientMessage(Component.translatable("gtextras.hatch.mana_status",
                        String.format("%,d", container.getStored()), String.format("%,d", container.getCapacity())),
                        true);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand,
                                                   net.minecraft.core.Direction gridSide, BlockHitResult hit) {
        if (container.getHandlerIO() == IO.OUT && player.isShiftKeyDown()) {
            if (!isRemote()) {
                player.displayClientMessage(container.cycleFluid(), true);
            }
            return InteractionResult.sidedSuccess(isRemote());
        }
        return super.onScrewdriverClick(player, hand, gridSide, hit);
    }
}
