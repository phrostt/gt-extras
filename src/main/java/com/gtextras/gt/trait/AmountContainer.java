package com.gtextras.gt.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public abstract class AmountContainer extends NotifiableRecipeHandlerTrait<Integer> {

    protected final IO handlerIO;
    protected final RecipeCapability<Integer> capability;
    protected final int capacity;
    protected final int transferPerOperation;
    protected final int operationInterval;
    protected final String nbtKey;

    protected int stored;
    protected int fluidIndex;
    private TickableSubscription tickSubscription;

    protected AmountContainer(MetaMachine machine, IO handlerIO, RecipeCapability<Integer> capability, int capacity,
                              int transferPerOperation, int operationInterval, String nbtKey) {
        super(machine);
        this.handlerIO = handlerIO;
        this.capability = capability;
        this.capacity = capacity;
        this.transferPerOperation = transferPerOperation;
        this.operationInterval = operationInterval;
        this.nbtKey = nbtKey;
    }

    protected abstract void transferWithNeighbours();

    protected abstract List<ExtrasFluids.Entry> fluidList();

    public net.minecraft.network.chat.Component cycleFluid() {
        List<ExtrasFluids.Entry> list = fluidList();
        if (list.isEmpty()) {
            return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_fluid_none");
        }
        fluidIndex = (Math.floorMod(fluidIndex, list.size()) + 1) % list.size();
        machine.markDirty();
        return currentFluidName();
    }

    public net.minecraft.network.chat.Component currentFluidName() {
        List<ExtrasFluids.Entry> list = fluidList();
        if (list.isEmpty()) {
            return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_fluid_none");
        }
        ExtrasFluids.Entry entry = list.get(Math.floorMod(fluidIndex, list.size()));
        return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_fluid",
                entry.fluid().getFluidType().getDescription(), String.format("%s", entry.ratio()));
    }

    public int getStored() {
        return stored;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getSpace() {
        return capacity - stored;
    }

    public void setStored(int value) {
        int clamped = Math.max(0, Math.min(capacity, value));
        if (clamped != stored) {
            stored = clamped;
            notifyListeners();
            machine.markDirty();
        }
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (!machine.isRemote()) {
            tickSubscription = machine.subscribeServerTick(this::tick);
        }
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        if (tickSubscription != null) {
            tickSubscription.unsubscribe();
            tickSubscription = null;
        }
    }

    private void tick() {
        if (machine.getOffsetTimer() % operationInterval == 0) {
            transferWithNeighbours();
        }
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        if (io != handlerIO) return left;
        long wanted = 0;
        for (Integer value : left) {
            if (value != null) wanted += value;
        }
        if (wanted <= 0) return null;

        long moved;
        if (io == IO.IN) {
            moved = Math.min(stored, wanted);
            if (!simulate && moved > 0) setStored((int) (stored - moved));
        } else {
            moved = Math.min(capacity - stored, wanted);
            if (!simulate && moved > 0) setStored((int) (stored + moved));
        }

        long remaining = wanted - moved;
        if (remaining <= 0) return null;
        reportShortfall(recipe, io, wanted, io == IO.IN ? stored : capacity - stored);
        List<Integer> result = new ArrayList<>(1);
        result.add((int) Math.min(remaining, Integer.MAX_VALUE));
        return result;
    }

    private void reportShortfall(GTRecipe recipe, IO io, long wanted, long available) {
        if (!(machine instanceof com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine part)) return;
        net.minecraft.network.chat.Component name = capability.getName();
        net.minecraft.network.chat.Component reason = net.minecraft.network.chat.Component.translatable(
                io == IO.IN ? "gtextras.recipe.insufficient_in" : "gtextras.recipe.insufficient_out", name,
                String.format("%,d", wanted), String.format("%,d", Math.max(0L, available)));
        for (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController controller : part.getControllers()) {
            if (controller instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine logicMachine) {
                com.gregtechceu.gtceu.api.machine.trait.RecipeLogic.putFailureReason(logicMachine.getRecipeLogic(),
                        recipe, reason);
            }
        }
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ArrayList<>(1);
        contents.add(stored);
        return contents;
    }

    @Override
    public double getTotalContentAmount() {
        return stored;
    }

    @Override
    public RecipeCapability<Integer> getCapability() {
        return capability;
    }

    @Override
    public IO getHandlerIO() {
        return handlerIO;
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putInt(nbtKey, stored);
        tag.putInt(nbtKey + "_fluid", fluidIndex);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        stored = Math.max(0, Math.min(capacity, tag.getInt(nbtKey)));
        fluidIndex = Math.max(0, tag.getInt(nbtKey + "_fluid"));
    }
}
