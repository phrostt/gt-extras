package com.gtextras.gt.will;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.trait.HatchUI;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WillHatchMachine extends TieredPartMachine implements IUIMachine {

    private final IO io;
    private final WillContainer[] containers = new WillContainer[ExtrasCapabilities.WILL_NAMES.length];

    public WillHatchMachine(IMachineBlockEntity holder, IO io, int tier) {
        super(holder, tier);
        this.io = io;
        for (int i = 0; i < containers.length; i++) {
            containers[i] = new WillContainer(this, io, tier, i);
        }
        for (int i = 1; i < containers.length; i++) {
            containers[i].setRawSource(containers[0]);
        }
    }

    public WillContainer[] containers() {
        return containers;
    }

    public IO io() {
        return io;
    }

    private void cycleOutput(Player player) {
        int current = 0;
        for (WillContainer container : containers) {
            if (container.outputActive()) current = container.typeIndex();
        }
        int next = (current + 1) % containers.length;
        for (WillContainer container : containers) {
            container.setOutputActive(container.typeIndex() == next);
        }
        String name = ExtrasCapabilities.WILL_NAMES[next];
        player.displayClientMessage(Component.translatable("gtextras.hatch.will_output_type",
                Character.toUpperCase(name.charAt(0)) + name.substring(1)), true);
    }

    private int activeOutput() {
        for (WillContainer container : containers) {
            if (container.outputActive()) return container.typeIndex();
        }
        return 0;
    }

    private void selectOutput(int index) {
        if (isRemote()) return;
        for (WillContainer container : containers) {
            container.setOutputActive(container.typeIndex() == index);
        }
        markDirty();
    }

    @Override
    public ModularUI createUI(Player player) {
        HatchUI.Panel panel = HatchUI.start(containers[0], "Raw", 0xFF7A3FB0);
        if (io == IO.OUT) {
            java.util.List<String> names = new java.util.ArrayList<>();
            for (String name : ExtrasCapabilities.WILL_NAMES) {
                names.add(Character.toUpperCase(name.charAt(0)) + name.substring(1));
            }
            HatchUI.choices(panel, "Output type", names, this::activeOutput, this::selectOutput);
            HatchUI.note(panel, "Fills tartaric gems first, then makes", "one crystal per cycle.");
        } else {
            HatchUI.note(panel, "Takes raw will from tartaric gems and", "crystals nearby.");
        }
        return HatchUI.finish(this, player, panel);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand,
                                                   net.minecraft.core.Direction gridSide, BlockHitResult hit) {
        if (io == IO.OUT) {
            if (!isRemote()) cycleOutput(player);
            return InteractionResult.sidedSuccess(isRemote());
        }
        return super.onScrewdriverClick(player, hand, gridSide, hit);
    }
}
