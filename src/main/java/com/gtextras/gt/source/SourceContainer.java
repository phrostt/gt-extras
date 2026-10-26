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

public class SourceContainer extends AmountContainer {

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
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
        if (handlerIO == IO.IN) {
            int moved = com.gtextras.gt.trait.ExtrasFluids.pull(level, machine.getPos(),
                    com.gtextras.gt.trait.ExtrasFluids.source(), Math.min(getSpace(), transferPerOperation));
            if (moved > 0) setStored(stored + moved);
        } else if (!nativeMode()) {
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
        } else if (nativeMode()) {
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
            }
        }
    }

    @Override
    protected boolean hasNative() {
        return true;
    }

    @Override
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.source();
    }
}
