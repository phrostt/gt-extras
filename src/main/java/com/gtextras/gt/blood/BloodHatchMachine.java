package com.gtextras.gt.blood;

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

public class BloodHatchMachine extends TieredPartMachine implements IUIMachine {

    private final BloodContainer container;

    public BloodHatchMachine(IMachineBlockEntity holder, IO io, int tier) {
        super(holder, tier);
        this.container = new BloodContainer(this, io, tier);
    }

    public BloodContainer container() {
        return container;
    }

    @Override
    public ModularUI createUI(Player player) {
        HatchUI.Panel panel = HatchUI.start(container, "Blood", 0xFFB01818);
        if (container.io() == IO.OUT) {
            java.util.List<String> names = new java.util.ArrayList<>();
            for (var entry : container.fluidChoices()) {
                net.minecraft.resources.ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.FLUIDS
                        .getKey(entry.fluid());
                names.add(key == null ? "?" : key.getPath().replace('_', ' ') + "  (" + key.getNamespace() + ")");
            }
            HatchUI.scrollChoices(panel, "Output fluid", 60, names, container::fluidIndex, container::selectFluid);
            HatchUI.note(panel, "Pushes into the tank in front of the hatch.");
        } else {
            HatchUI.note(panel, "Pulls from tanks next to the hatch.");
        }
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
