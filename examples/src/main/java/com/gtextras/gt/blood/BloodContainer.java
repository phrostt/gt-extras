package com.gtextras.gt.blood;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.trait.AmountContainer;
import com.gtextras.gt.trait.HatchConfig;
import com.gtextras.gt.trait.ExtrasFluids;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class BloodContainer extends AmountContainer implements com.gtextras.gt.trait.Linkable {

    public static final int INTERVAL = 10;
    public static final int RANGE = 10;

    private final com.gtextras.gt.trait.LinkSet links = new com.gtextras.gt.trait.LinkSet();

    public static int transferFor(int tier) {
        return Math.max(1, HatchConfig.rate("blood", tier) * INTERVAL);
    }

    public static int capacityFor(int tier) {
        return HatchConfig.storage("blood", tier);
    }

    public BloodContainer(MetaMachine machine, IO io, int tier) {
        super(machine, io, ExtrasCapabilities.BLOOD, capacityFor(tier), transferFor(tier), INTERVAL,
                "gtextras_blood");
        configureCost("blood", tier);
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
        BlockPos pos = machine.getPos();
        int budget = transferPerOperation;
        if (handlerIO == IO.IN) {
            int moved = ExtrasFluids.pull(level, pos, ExtrasFluids.blood(), Math.min(getSpace(), budget));
            if (moved > 0) {
                setStored(stored + moved);
                budget -= moved;
            }
            if (budget > 0) {
                int eaten = ExtrasFluids.eat(level, pos, ExtrasFluids.blood(), ExtrasFluids.bloodItems(),
                        Math.min(getSpace(), budget));
                if (eaten > 0) {
                    setStored(stored + eaten);
                    budget -= eaten;
                }
            }
            if (budget > 0 && !links.isEmpty()) pullLinked(level, budget);
        } else {
            int moved = ExtrasFluids.push(level, pos, ExtrasFluids.blood(), Math.min(stored, transferPerOperation), fluidIndex);
            if (moved > 0) {
                setStored(stored - moved);
                budget -= moved;
            }
            if (budget > 0 && !links.isEmpty()) pushLinked(level, budget);
        }
    }

    private static net.minecraftforge.fluids.capability.IFluidHandler handlerAt(Level level, BlockPos target) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(target);
        if (be == null) return null;
        net.minecraftforge.fluids.capability.IFluidHandler handler = be
                .getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, null)
                .resolve().orElse(null);
        if (handler != null) return handler;
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.values()) {
            handler = be.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.FLUID_HANDLER, dir)
                    .resolve().orElse(null);
            if (handler != null) return handler;
        }
        return null;
    }

    private void pullLinked(Level level, int budget) {
        BlockPos pos = machine.getPos();
        java.util.List<ExtrasFluids.Entry> fluids = ExtrasFluids.blood();
        for (BlockPos target : links.ordered()) {
            if (getSpace() <= 0 || budget <= 0) return;
            if (target.distSqr(pos) > (double) RANGE * RANGE || !level.isLoaded(target)) continue;
            net.minecraftforge.fluids.capability.IFluidHandler handler = handlerAt(level, target);
            if (handler == null) continue;
            int units = 0;
            for (ExtrasFluids.Entry entry : fluids) {
                int room = Math.min(getSpace(), budget) - units;
                if (room <= 0) break;
                int mb = (int) Math.min(Integer.MAX_VALUE, Math.max(1L, (long) (room / entry.ratio())));
                net.minecraftforge.fluids.FluidStack drained = handler.drain(
                        new net.minecraftforge.fluids.FluidStack(entry.fluid(), mb),
                        net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                if (!drained.isEmpty()) units += (int) Math.round(drained.getAmount() * entry.ratio());
            }
            if (units > 0) {
                setStored(stored + units);
                budget -= units;
                links.advance();
                com.gtextras.gt.trait.TransferFx.orb(level, target, pos, com.gtextras.gt.trait.TransferFx.BLOOD);
            }
        }
    }

    private void pushLinked(Level level, int budget) {
        BlockPos pos = machine.getPos();
        java.util.List<ExtrasFluids.Entry> fluids = ExtrasFluids.blood();
        if (fluids.isEmpty()) return;
        ExtrasFluids.Entry entry = fluids.get(Math.floorMod(fluidIndex, fluids.size()));
        for (BlockPos target : links.ordered()) {
            if (stored <= 0 || budget <= 0) return;
            if (target.distSqr(pos) > (double) RANGE * RANGE || !level.isLoaded(target)) continue;
            net.minecraftforge.fluids.capability.IFluidHandler handler = handlerAt(level, target);
            if (handler == null) continue;
            int mb = (int) Math.min(Integer.MAX_VALUE, (long) (Math.min(stored, budget) / entry.ratio()));
            if (mb <= 0) continue;
            int filled = handler.fill(new net.minecraftforge.fluids.FluidStack(entry.fluid(), mb),
                    net.minecraftforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            int units = (int) Math.round(filled * entry.ratio());
            if (units > 0) {
                setStored(stored - units);
                budget -= units;
                links.advance();
                com.gtextras.gt.trait.TransferFx.orb(level, pos, target, com.gtextras.gt.trait.TransferFx.BLOOD);
            }
        }
    }

    @Override
    public boolean canLink(Level level, BlockPos target) {
        return level.getBlockEntity(target) instanceof wayoftime.bloodmagic.altar.IBloodAltar;
    }

    @Override
    public int linkRange() {
        return RANGE;
    }

    @Override
    public int[] linkColor() {
        return com.gtextras.gt.trait.TransferFx.BLOOD;
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

    @Override
    protected java.util.List<com.gtextras.gt.trait.ExtrasFluids.Entry> fluidList() {
        return com.gtextras.gt.trait.ExtrasFluids.blood();
    }
}
