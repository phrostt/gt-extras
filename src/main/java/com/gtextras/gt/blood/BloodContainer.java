package com.gtextras.gt.blood;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.trait.AmountContainer;
import com.gtextras.gt.trait.HatchConfig;
import com.gtextras.gt.trait.ExtrasFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BloodContainer extends AmountContainer {

    public static final int INTERVAL = 10;

    public static int transferFor(int tier) {
        return Math.max(1, HatchConfig.rate("blood", tier) * INTERVAL);
    }

    public static int capacityFor(int tier) {
        return HatchConfig.storage("blood", tier);
    }

    public BloodContainer(MetaMachine machine, IO io, int tier) {
        super(machine, io, ExtrasCapabilities.BLOOD, capacityFor(tier), transferFor(tier), INTERVAL,
                "gtextras_blood");
<<<<<<< HEAD
        configureCost("blood", tier);
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
        BlockPos pos = machine.getPos();
        if (handlerIO == IO.IN) {
<<<<<<< HEAD
            int budget = transferPerOperation;
            int moved = ExtrasFluids.pull(level, pos, ExtrasFluids.blood(), Math.min(getSpace(), budget));
            if (moved > 0) {
                setStored(stored + moved);
                budget -= moved;
            }
            if (budget > 0) {
                int eaten = ExtrasFluids.eat(level, pos, ExtrasFluids.blood(), ExtrasFluids.bloodItems(),
                        Math.min(getSpace(), budget));
                if (eaten > 0) setStored(stored + eaten);
            }
=======
            int moved = ExtrasFluids.pull(level, pos, ExtrasFluids.blood(),
                    Math.min(getSpace(), transferPerOperation));
            if (moved > 0) setStored(stored + moved);
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
        } else {
            int moved = ExtrasFluids.push(level, pos, ExtrasFluids.blood(), Math.min(stored, transferPerOperation), fluidIndex);
            if (moved > 0) setStored(stored - moved);
        }
    }

    @Override
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.blood();
    }
}
