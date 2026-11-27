package com.gtextras.gt.mana;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import com.gtextras.gt.trait.HatchUI;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ManaHatchMachine extends TieredPartMachine implements IUIMachine {

    private final ManaContainer container;

    public ManaHatchMachine(IMachineBlockEntity holder, IO io, int tier) {
        super(holder, tier);
        this.container = new ManaContainer(this, io, tier);
    }

    public ManaContainer container() {
        return container;
    }

    @Override
    public ModularUI createUI(Player player) {
        HatchUI.Panel panel = HatchUI.start(container, "Mana", 0xFF46AAFF);
        HatchUI.links(panel, container, "Link Tool or Wand of the Forest", container.io() == IO.OUT ? "Linked targets" : "Linked sources");
        return HatchUI.finish(this, player, panel);
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
