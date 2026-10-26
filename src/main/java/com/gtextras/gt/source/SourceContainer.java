package com.gtextras.gt.source;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.trait.AmountContainer;
import com.gtextras.gt.trait.HatchConfig;

import com.hollingsworth.arsnouveau.api.source.ISourceTile;
import com.hollingsworth.arsnouveau.api.source.ISpecialSourceProvider;
import com.hollingsworth.arsnouveau.api.util.SourceUtil;

import net.minecraft.world.level.Level;

import java.util.List;

<<<<<<< HEAD
public class SourceContainer extends AmountContainer implements com.gtextras.gt.trait.Linkable {
=======
public class SourceContainer extends AmountContainer {
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

    public static int transferFor(int tier) {
        return Math.max(1, HatchConfig.rate("source", tier) * INTERVAL);
    }

    public static int capacityFor(int tier) {
        return HatchConfig.storage("source", tier);
    }

    public static final int INTERVAL = 10;
    public static final int RANGE = 6;

    public SourceContainer(MetaMachine machine, IO io, int tier) {
        super(machine, io, ExtrasCapabilities.SOURCE, capacityFor(tier), transferFor(tier), INTERVAL, "gtextras_source");
<<<<<<< HEAD
        configureCost("source", tier);
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
<<<<<<< HEAD
        int budget = transferPerOperation;
        if (handlerIO == IO.IN) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.pull(level, machine.getPos(),
                    com.gtextras.gt.trait.ExtrasFluids.source(), Math.min(getSpace(), budget));
            if (moved > 0) {
                setStored(stored + moved);
                budget -= moved;
            }
            if (budget > 0) {
                int eaten = com.gtextras.gt.trait.ExtrasFluids.eat(level, machine.getPos(),
                        com.gtextras.gt.trait.ExtrasFluids.source(), com.gtextras.gt.trait.ExtrasFluids.sourceItems(),
                        Math.min(getSpace(), budget));
                if (eaten > 0) {
                    setStored(stored + eaten);
                    budget -= eaten;
                }
            }
        } else if (links.isEmpty() && com.gtextras.gt.trait.ExtrasFluids.hasTank(level, machine.getPos())) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.push(level, machine.getPos(),
                    com.gtextras.gt.trait.ExtrasFluids.source(), Math.min(stored, transferPerOperation),
                    Math.min(fluidIndex, Math.max(0, fluidList().size() - 1)));
            if (moved > 0) setStored(stored - moved);
            return;
        }
        if (budget <= 0 || links.isEmpty()) return;
        if (handlerIO == IO.IN) {
            pullLinked(level, budget);
        } else {
            pushLinked(level, budget);
        }
    }

    private void pullLinked(Level level, int budget) {
        net.minecraft.core.BlockPos pos = machine.getPos();
        for (net.minecraft.core.BlockPos target : links.ordered()) {
            if (getSpace() <= 0 || budget <= 0) return;
            if (target.distSqr(pos) > RANGE * RANGE || !level.isLoaded(target)) continue;
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(target);
            if (!(be instanceof ISourceTile tile)) continue;
            boolean infinite = creative(tile);
            int want = Math.min(Math.min(infinite ? Integer.MAX_VALUE : tile.getSource(), getSpace()), budget);
            if (want <= 0) continue;
            int taken = infinite ? want : extract(tile, want);
            if (taken > 0) {
                setStored(stored + taken);
                budget -= taken;
                links.advance();
                com.gtextras.gt.trait.TransferFx.orb(level, target, pos, com.gtextras.gt.trait.TransferFx.SOURCE);
=======
        if (handlerIO == IO.IN) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.pull(level, machine.getPos(),
                    com.gtextras.gt.trait.ExtrasFluids.source(), Math.min(getSpace(), transferPerOperation));
            if (moved > 0) setStored(stored + moved);
        } else {
            int moved = com.gtextras.gt.trait.ExtrasFluids.push(level, machine.getPos(),
                    com.gtextras.gt.trait.ExtrasFluids.source(), Math.min(stored, transferPerOperation), fluidIndex);
            if (moved > 0) setStored(stored - moved);
        }
        int budget = transferPerOperation;

        if (handlerIO == IO.IN) {
            if (getSpace() <= 0) return;
            List<ISpecialSourceProvider> providers = SourceUtil.canTakeSource(machine.getPos(), level, RANGE);
            for (ISpecialSourceProvider provider : providers) {
                if (budget <= 0 || getSpace() <= 0) break;
                ISourceTile tile = provider.getSource();
                if (tile == null) continue;
                int want = Math.min(Math.min(tile.getSource(), getSpace()), budget);
                if (want <= 0) continue;
                int before = tile.getSource();
                tile.removeSource(want);
                int taken = before - tile.getSource();
                if (taken > 0) {
                    setStored(stored + taken);
                    budget -= taken;
                }
            }
        } else {
            if (stored <= 0) return;
            List<ISpecialSourceProvider> providers = SourceUtil.canGiveSource(machine.getPos(), level, RANGE);
            for (ISpecialSourceProvider provider : providers) {
                if (budget <= 0 || stored <= 0) break;
                ISourceTile tile = provider.getSource();
                if (tile == null) continue;
                int space = tile.getMaxSource() - tile.getSource();
                int give = Math.min(Math.min(space, stored), budget);
                if (give <= 0) continue;
                int before = tile.getSource();
                tile.addSource(give);
                int given = tile.getSource() - before;
                if (given > 0) {
                    setStored(stored - given);
                    budget -= given;
                }
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
            }
        }
    }

<<<<<<< HEAD
    private final com.gtextras.gt.trait.LinkSet links = new com.gtextras.gt.trait.LinkSet();

    @Override
    public boolean canLink(Level level, net.minecraft.core.BlockPos target) {
        return level.getBlockEntity(target) instanceof ISourceTile;
    }

    @Override
    public int linkRange() {
        return RANGE;
    }

    @Override
    public int[] linkColor() {
        return com.gtextras.gt.trait.TransferFx.SOURCE;
    }

    @Override
    public boolean linkable() {
        return true;
    }

    @Override
    public net.minecraft.core.BlockPos linkOrigin() {
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

    private void pushLinked(Level level, int budget) {
        net.minecraft.core.BlockPos pos = machine.getPos();
        for (net.minecraft.core.BlockPos target : links.ordered()) {
            if (stored <= 0 || budget <= 0) return;
            if (target.distSqr(pos) > RANGE * RANGE || !level.isLoaded(target)) continue;
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(target);
            if (!(be instanceof ISourceTile tile)) continue;
            boolean sink = creative(tile);
            int space = sink ? Integer.MAX_VALUE : tile.getMaxSource() - tile.getSource();
            int give = Math.min(Math.min(space, stored), budget);
            if (give <= 0) continue;
            int given = sink ? give : insert(tile, give);
            if (given > 0) {
                setStored(stored - given);
                budget -= given;
                links.advance();
                com.gtextras.gt.trait.TransferFx.orb(level, pos, target, com.gtextras.gt.trait.TransferFx.SOURCE);
            }
        }
    }

    private static int extract(ISourceTile tile, int want) {
        int before = tile.getSource();
        int returned = tile.removeSource(want);
        int diff = before - tile.getSource();
        if (diff > 0) return Math.min(diff, want);
        return Math.min(Math.max(returned, 0), want);
    }

    private static int insert(ISourceTile tile, int give) {
        int before = tile.getSource();
        int returned = tile.addSource(give);
        int diff = tile.getSource() - before;
        if (diff > 0) return Math.min(diff, give);
        return Math.min(Math.max(returned, 0), give);
    }

    private static boolean creative(ISourceTile tile) {
        return tile.getClass().getSimpleName().contains("Creative");
    }

    @Override
    protected boolean hasNative() {
        return false;
    }

=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    @Override
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.source();
    }
}
