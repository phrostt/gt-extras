package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;

import net.minecraft.world.entity.player.Player;

public class BridgeHatchMachine extends TieredPartMachine implements IUIMachine {

    private final BridgeContainer container;

    public BridgeHatchMachine(IMachineBlockEntity holder, IO io, int tier, BridgeDef def) {
        super(holder, tier);
        this.container = new BridgeContainer(this, io, tier, def);
    }

    public BridgeContainer container() {
        return container;
    }

    @Override
    public ModularUI createUI(Player player) {
        return BridgeUI.create(this, player, container);
    }
}
