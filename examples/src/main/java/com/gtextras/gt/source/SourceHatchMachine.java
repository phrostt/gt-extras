package com.gtextras.gt.source;

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

public class SourceHatchMachine extends TieredPartMachine implements IUIMachine {

    private final SourceContainer container;

    public SourceHatchMachine(IMachineBlockEntity holder, IO io, int tier) {
        super(holder, tier);
        this.container = new SourceContainer(this, io, tier);
    }

    public SourceContainer container() {
        return container;
    }

    @Override
    public ModularUI createUI(Player player) {
        HatchUI.Panel panel = HatchUI.start(container, "Source", 0xFFFF28BE);
        HatchUI.links(panel, container, "Link Tool or Dominion Wand", container.io() == IO.OUT ? "Linked targets" : "Linked sources");
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
