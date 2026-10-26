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
        configureCost("mana", tier);
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
            int eaten = com.gtextras.gt.trait.ExtrasFluids.eat(level, pos,
                    com.gtextras.gt.trait.ExtrasFluids.mana(), com.gtextras.gt.trait.ExtrasFluids.manaItems(),
                    Math.min(getSpace(), transferPerOperation));
            if (eaten > 0) setStored(stored + eaten);
        } else if (boundPool == null && com.gtextras.gt.trait.ExtrasFluids.hasTank(level, pos)) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.push(level, pos, com.gtextras.gt.trait.ExtrasFluids.mana(),
                    Math.min(stored, transferPerOperation), Math.min(fluidIndex, Math.max(0, fluidList().size() - 1)));
            if (moved > 0) setStored(stored - moved);
            return;
        }
        int budget = transferPerOperation;
        if (handlerIO == IO.OUT) {
            if (boundPool != null) {
                pushBound(level, pos, budget);
                return;
            }
        }
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
                    com.gtextras.gt.trait.TransferFx.orb(level, be.getBlockPos(), pos,
                            com.gtextras.gt.trait.TransferFx.MANA);
                }
            } else {
                if (other.isFull()) continue;
                int move = Math.min(stored, budget);
                if (move <= 0) continue;
                int before = other.getCurrentMana();
                other.receiveMana(move);
                int given = other.getCurrentMana() - before;
                if (given > 0) {
                    setStored(stored - given);
                    budget -= given;
                    com.gtextras.gt.trait.TransferFx.orb(level, pos, be.getBlockPos(),
                            com.gtextras.gt.trait.TransferFx.MANA);
                }
            }
        }
    }

    public static final int BIND_RANGE = 6;
    private BlockPos boundPool;

    public BlockPos boundPool() {
        return boundPool;
    }

    public void bindPool(BlockPos pos) {
        boundPool = pos;
        machine.markDirty();
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (boundPool != null) tag.putLong(nbtKey + "_pool", boundPool.asLong());
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        boundPool = tag.contains(nbtKey + "_pool") ? BlockPos.of(tag.getLong(nbtKey + "_pool")) : null;
    }

    private int pushBound(Level level, BlockPos pos, int budget) {
        if (boundPool == null || stored <= 0 || budget <= 0) return budget;
        if (boundPool.distSqr(pos) > BIND_RANGE * BIND_RANGE || !level.isLoaded(boundPool)) return budget;
        BlockEntity be = level.getBlockEntity(boundPool);
        if (be == null) return budget;
        ManaReceiver target = be.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER).resolve().orElse(null);
        if (target == null || target.isFull()) return budget;
        int move = Math.min(stored, budget);
        int before = target.getCurrentMana();
        target.receiveMana(move);
        int given = target.getCurrentMana() - before;
        if (given > 0) {
            setStored(stored - given);
            budget -= given;
            com.gtextras.gt.trait.TransferFx.orb(level, pos, boundPool, com.gtextras.gt.trait.TransferFx.MANA);
        }
        return budget;
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
        return false;
    }

    @Override
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.mana();
    }
}
