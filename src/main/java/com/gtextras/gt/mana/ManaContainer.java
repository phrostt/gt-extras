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

<<<<<<< HEAD
public class ManaContainer extends AmountContainer implements ManaReceiver, com.gtextras.gt.trait.Linkable {
=======
public class ManaContainer extends AmountContainer implements ManaReceiver {
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

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
<<<<<<< HEAD
        configureCost("mana", tier);
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
        BlockPos pos = machine.getPos();
<<<<<<< HEAD
        int budget = transferPerOperation;
        if (handlerIO == IO.IN) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.pull(level, pos, com.gtextras.gt.trait.ExtrasFluids.mana(),
                    Math.min(getSpace(), budget));
            if (moved > 0) {
                setStored(stored + moved);
                budget -= moved;
            }
            if (budget > 0) {
                int eaten = com.gtextras.gt.trait.ExtrasFluids.eat(level, pos,
                        com.gtextras.gt.trait.ExtrasFluids.mana(), com.gtextras.gt.trait.ExtrasFluids.manaItems(),
                        Math.min(getSpace(), budget));
                if (eaten > 0) {
                    setStored(stored + eaten);
                    budget -= eaten;
                }
            }
        } else if (links.isEmpty() && com.gtextras.gt.trait.ExtrasFluids.hasTank(level, pos)) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.push(level, pos, com.gtextras.gt.trait.ExtrasFluids.mana(),
                    Math.min(stored, transferPerOperation), Math.min(fluidIndex, Math.max(0, fluidList().size() - 1)));
            if (moved > 0) setStored(stored - moved);
            return;
        }
        if (budget <= 0 || links.isEmpty()) return;
        if (handlerIO == IO.OUT) {
            pushLinked(level, pos, budget);
        } else {
            pullLinked(level, pos, budget);
        }
    }

    private static boolean creative(BlockEntity be) {
        net.minecraft.resources.ResourceLocation key = net.minecraftforge.registries.ForgeRegistries.BLOCKS
                .getKey(be.getBlockState().getBlock());
        return key != null && key.getPath().contains("creative");
    }

    private void pullLinked(Level level, BlockPos pos, int budget) {
        for (BlockPos target : links.ordered()) {
            if (getSpace() <= 0 || budget <= 0) return;
            if (target.distSqr(pos) > BIND_RANGE * BIND_RANGE || !level.isLoaded(target)) continue;
            BlockEntity be = level.getBlockEntity(target);
            if (be == null) continue;
            ManaReceiver other = be.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER).resolve().orElse(null);
            if (other == null || other == this) continue;
            boolean infinite = creative(be);
            int move = Math.min(Math.min(infinite ? Integer.MAX_VALUE : other.getCurrentMana(), getSpace()), budget);
            if (move <= 0) continue;
            int taken;
            if (infinite) {
                taken = move;
            } else {
                int before = other.getCurrentMana();
                other.receiveMana(-move);
                taken = before - other.getCurrentMana();
            }
            if (taken > 0) {
                setStored(stored + taken);
                budget -= taken;
                links.advance();
                com.gtextras.gt.trait.TransferFx.orb(level, target, pos, com.gtextras.gt.trait.TransferFx.MANA);
            }
        }
    }

    public static final int BIND_RANGE = 6;
    private final com.gtextras.gt.trait.LinkSet links = new com.gtextras.gt.trait.LinkSet();

    @Override
    public boolean canLink(Level level, BlockPos target) {
        BlockEntity be = level.getBlockEntity(target);
        return be != null && be.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER).resolve().isPresent();
    }

    @Override
    public int linkRange() {
        return BIND_RANGE;
    }

    @Override
    public int[] linkColor() {
        return com.gtextras.gt.trait.TransferFx.MANA;
    }

    @Override
    public boolean linkable() {
        return true;
    }

    @Override
    public BlockPos linkOrigin() {
        return machine.getPos();
    }

    @Override
    public com.gtextras.gt.trait.LinkSet links() {
        return links;
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        links.save(tag, nbtKey + "_links");
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        links.load(tag, nbtKey + "_links");
    }

    private void pushLinked(Level level, BlockPos pos, int budget) {
        for (BlockPos target : links.ordered()) {
            if (stored <= 0 || budget <= 0) return;
            if (target.distSqr(pos) > BIND_RANGE * BIND_RANGE || !level.isLoaded(target)) continue;
            BlockEntity be = level.getBlockEntity(target);
            if (be == null) continue;
            ManaReceiver receiver = be.getCapability(BotaniaForgeCapabilities.MANA_RECEIVER).resolve().orElse(null);
            if (receiver == null) continue;
            boolean sink = creative(be);
            if (!sink && receiver.isFull()) continue;
            int move = Math.min(stored, budget);
            int given;
            if (sink) {
                given = move;
            } else {
                int before = receiver.getCurrentMana();
                receiver.receiveMana(move);
                given = receiver.getCurrentMana() - before;
            }
            if (given > 0) {
                setStored(stored - given);
                budget -= given;
                links.advance();
                com.gtextras.gt.trait.TransferFx.orb(level, pos, target, com.gtextras.gt.trait.TransferFx.MANA);
=======
        if (handlerIO == IO.IN) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.pull(level, pos, com.gtextras.gt.trait.ExtrasFluids.mana(),
                    Math.min(getSpace(), transferPerOperation));
            if (moved > 0) setStored(stored + moved);
        } else {
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
                if (other.isFull()) continue;
                int move = Math.min(stored, budget);
                if (move <= 0) continue;
                int before = other.getCurrentMana();
                other.receiveMana(move);
                int given = other.getCurrentMana() - before;
                if (given > 0) {
                    setStored(stored - given);
                    budget -= given;
                }
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
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
<<<<<<< HEAD
    protected boolean hasNative() {
        return false;
    }

    @Override
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.mana();
    }
}
