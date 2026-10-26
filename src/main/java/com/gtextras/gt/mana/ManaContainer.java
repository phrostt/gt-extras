package com.gtextras.gt.mana;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.trait.AmountContainer;
import com.gtextras.gt.trait.HatchConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;

public class ManaContainer extends AmountContainer implements ManaReceiver {

    public static final int INTERVAL = 10;

    public static int transferFor(int tier) {
        return Math.max(1, HatchConfig.rate("mana", tier) * INTERVAL);
    }

    public static int capacityFor(int tier) {
        return HatchConfig.storage("mana", tier);
    }

    public ManaContainer(MetaMachine machine, IO io, int tier) {
        super(machine, io, ExtrasCapabilities.MANA, capacityFor(tier), transferFor(tier), INTERVAL,
                "gtextras_mana");
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
        BlockPos pos = machine.getPos();
        if (handlerIO == IO.IN) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.pull(level, pos, com.gtextras.gt.trait.ExtrasFluids.mana(),
                    Math.min(getSpace(), transferPerOperation));
            if (moved > 0) setStored(stored + moved);
        } else if (!nativeMode()) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.push(level, pos, com.gtextras.gt.trait.ExtrasFluids.mana(),
                    Math.min(stored, transferPerOperation), fluidIndex);
            if (moved > 0) setStored(stored - moved);
        }
        int budget = transferPerOperation;
        for (Direction dir : Direction.values()) {
            if (budget <= 0) break;
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be == null || be instanceof com.gregtechceu.gtceu.api.machine.IMachineBlockEntity) continue;
            ManaReceiver other = be.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER, dir.getOpposite())
                    .resolve().orElse(null);
            if (other == null || other == this) continue;

            if (handlerIO == IO.IN) {
                if (!(other instanceof ManaPool)) continue;
                int move = Math.min(Math.min(other.getCurrentMana(), getSpace()), budget);
                if (move <= 0) continue;
                int before = other.getCurrentMana();
                other.receiveMana(-move);
                int taken = before - other.getCurrentMana();
                if (taken > 0) {
                    setStored(stored + taken);
                    budget -= taken;
                }
            } else {
                if (!nativeMode() || other.isFull()) continue;
                int move = Math.min(stored, budget);
                if (move <= 0) continue;
                int before = other.getCurrentMana();
                other.receiveMana(move);
                int given = other.getCurrentMana() - before;
                if (given > 0) {
                    setStored(stored - given);
                    budget -= given;
                }
            }
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return machine.getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return machine.getPos();
    }

    @Override
    public int getCurrentMana() {
        return stored;
    }

    @Override
    public boolean isFull() {
        return stored >= capacity;
    }

    @Override
    public void receiveMana(int mana) {
        setStored(stored + mana);
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return handlerIO == IO.IN;
    }

    @Override
    protected boolean hasNative() {
        return true;
    }

    @Override
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.mana();
    }
}
