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

    public static void init() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, ManaCaps::attach);
    }

    private static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof MetaMachineBlockEntity be) {
            event.addCapability(ID, new Provider(be));
        }
    }

    private static final class Provider implements ICapabilityProvider {

        private final MetaMachineBlockEntity be;

        private Provider(MetaMachineBlockEntity be) {
            this.be = be;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
                MetaMachine machine = be.getMetaMachine();
                if (machine instanceof ManaHatchMachine hatch) {
                    return LazyOptional.of(hatch::container).cast();
                }
            }
            return LazyOptional.empty();
        }
    }
}
