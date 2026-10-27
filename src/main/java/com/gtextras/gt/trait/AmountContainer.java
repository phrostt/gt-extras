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
    protected final int baseCapacity;
    protected int capacity;
    protected final int transferPerOperation;
    protected final int operationInterval;
    protected final String nbtKey;

    protected int stored;
    private int lastSeen;
    private boolean grouped;
    protected int fluidIndex;
    private TickableSubscription tickSubscription;
    private boolean passive = true;
    private long demandUntil;

    public boolean passive() {
        return passive;
    }

    public void setPassive(boolean value) {
        if (machine.isRemote()) return;
        for (var trait : machine.getTraits()) {
            if (trait instanceof AmountContainer other) other.passive = value;
        }
        machine.markDirty();
    }

    public int costPerOperation() {
        return costResource == null ? 0 : HatchConfig.cost(costResource, costTier);
    }

    public int operationInterval() {
        return operationInterval;
    }

    private boolean demanded() {
        net.minecraft.world.level.Level level = machine.getLevel();
        if (level != null && level.getGameTime() < demandUntil) return true;
        var controller = formedController();
        return controller instanceof com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine logic
                && (logic.getRecipeLogic().isWorking() || logic.getRecipeLogic().isWaiting());
    }

    protected AmountContainer(MetaMachine machine, IO handlerIO, RecipeCapability<Integer> capability, int capacity,
                              int transferPerOperation, int operationInterval, String nbtKey) {
        super(machine);
        this.handlerIO = handlerIO;
        this.capability = capability;
        this.baseCapacity = capacity;
        this.capacity = capacity;
        this.transferPerOperation = transferPerOperation;
        this.operationInterval = operationInterval;
        this.nbtKey = nbtKey;
        new AmountFluidHandler(machine, this);
    }

    protected abstract void transferWithNeighbours();

    protected abstract List<ExtrasFluids.Entry> fluidList();

    protected boolean hasNative() {
        return false;
    }

    protected boolean nativeMode() {
        return handlerIO == IO.OUT && hasNative() && fluidIndex == fluidList().size();
    }

    public net.minecraft.network.chat.Component cycleFluid() {
        List<ExtrasFluids.Entry> list = fluidList();
        int total = list.size() + (hasNative() ? 1 : 0);
        if (total == 0) {
            return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_fluid_none");
        }
        fluidIndex = (Math.floorMod(fluidIndex, total) + 1) % total;
        machine.markDirty();
        return currentFluidName();
    }

    public net.minecraft.network.chat.Component currentFluidName() {
        List<ExtrasFluids.Entry> list = fluidList();
        if (list.isEmpty() && !hasNative()) {
            return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_fluid_none");
        }
        if (hasNative() && (list.isEmpty() || Math.floorMod(fluidIndex, list.size() + 1) == list.size())) {
            return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_native",
                    capability.getName());
        }
        ExtrasFluids.Entry entry = list.get(Math.floorMod(fluidIndex, list.size()));
        return net.minecraft.network.chat.Component.translatable("gtextras.hatch.output_fluid",
                entry.fluid().getFluidType().getDescription(), String.format("%s", entry.ratio()));
    }

    public int maxPerTick() {
        return Math.max(1, transferPerOperation / Math.max(1, operationInterval));
    }

    public void emptyBuffer() {
        if (machine.isRemote()) return;
        stored = 0;
        sync();
        notifyListeners();
        machine.markDirty();
    }

    public int fluidIndex() {
        return fluidIndex;
    }

    public void selectFluid(int index) {
        int size = fluidList().size();
        if (size == 0) return;
        fluidIndex = Math.max(0, Math.min(size - 1, index));
        machine.markDirty();
    }

    public List<ExtrasFluids.Entry> fluidChoices() {
        return fluidList();
    }

    public MetaMachine owner() {
        return machine;
    }

    public IO io() {
        return handlerIO;
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

    public void markLinksDirty() {
        machine.markDirty();
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

    private String costResource;
    private int costTier;

    public void configureCost(String resource, int tier) {
        costResource = resource;
        costTier = tier;
    }
    private java.util.List<com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer> controllerEnergy() {
        java.util.List<com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer> out = new ArrayList<>();
        if (!(machine instanceof com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine part)) {
            return out;
        }
        for (var controller : part.getControllers()) {
            if (!controller.isFormed()) continue;
            for (var other : controller.getParts()) {
                if (!(other instanceof MetaMachine meta)) continue;
                for (var trait : meta.getTraits()) {
                    if (trait instanceof com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer found
                            && found.getHandlerIO() == IO.IN && !out.contains(found)) {
                        out.add(found);
                    }
                }
            }
        }
        return out;
    }

    private com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController formedController() {
        if (!(machine instanceof com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine part)) {
            return null;
        }
        for (var controller : part.getControllers()) {
            if (controller.isFormed()) return controller;
        }
        return null;
    }

    private java.util.List<AmountContainer> groupMembers(
            com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController controller) {
        java.util.List<AmountContainer> out = new ArrayList<>();
        for (var other : controller.getParts()) {
            if (!(other instanceof MetaMachine meta)) continue;
            for (var trait : meta.getTraits()) {
                if (trait instanceof AmountContainer found && found.capability == capability) {
                    out.add(found);
                }
            }
        }
        return out;
    }

    protected final java.util.List<AmountContainer> groupPeers() {
        var controller = formedController();
        if (controller == null) return java.util.List.of();
        return groupMembers(controller);
    }

    private static final String POOL_TAG = "gtextras_pool";

    protected final void sync() {
        if (machine.isRemote()) return;
        var controller = formedController();
        if (controller == null) return;
        net.minecraft.world.level.Level level = controller.self().getLevel();
        if (level == null) return;
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(controller.self().getPos());
        if (be == null) return;
        java.util.List<AmountContainer> group = groupMembers(controller);
        if (!group.contains(this)) return;
        net.minecraft.nbt.CompoundTag root = be.getPersistentData();
        net.minecraft.nbt.CompoundTag pools = root.getCompound(POOL_TAG);
        int cap = Integer.MAX_VALUE;
        long total = pools.getInt(nbtKey);
        for (AmountContainer member : group) {
            cap = Math.min(cap, member.baseCapacity);
            total += member.stored - member.lastSeen;
        }
        int pool = (int) Math.max(0L, Math.min(cap, total));
        if (pools.getInt(nbtKey) != pool || !root.contains(POOL_TAG)) {
            pools.putInt(nbtKey, pool);
            root.put(POOL_TAG, pools);
            be.setChanged();
        }
        for (AmountContainer member : group) {
            boolean changed = member.stored != pool;
            member.capacity = cap;
            member.stored = pool;
            member.lastSeen = pool;
            member.grouped = true;
            if (changed) {
                member.notifyListeners();
                member.machine.markDirty();
            }
        }
    }

    private boolean inFormedMultiblock() {
        if (!(machine instanceof com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine part)) {
            return false;
        }
        for (var controller : part.getControllers()) {
            if (controller.isFormed()) return true;
        }
        return false;
    }

    private boolean hasPower(long cost) {
        long total = 0;
        for (var container : controllerEnergy()) {
            total += container.getEnergyStored();
        }
        return total >= cost;
    }

    private void spendPower(long cost) {
        long left = cost;
        for (var container : controllerEnergy()) {
            if (left <= 0) break;
            long take = Math.min(left, container.getEnergyStored());
            if (take > 0) {
                container.changeEnergy(-take);
                left -= take;
            }
        }
    }

    private void tick() {
        if (machine.getOffsetTimer() % operationInterval != 0) return;
        if (!inFormedMultiblock()) {
            if (grouped) {
                grouped = false;
                capacity = baseCapacity;
                stored = 0;
                lastSeen = 0;
                notifyListeners();
                machine.markDirty();
            }
            return;
        }
        sync();
        if (!passive && !demanded()) return;
        long cost = costResource == null ? 0 : HatchConfig.cost(costResource, costTier);
        if (cost <= 0) {
            transferWithNeighbours();
            return;
        }
        if (!hasPower(cost)) return;
        int before = stored;
        transferWithNeighbours();
        if (stored != before) spendPower(cost);
    }

    @Override
    public List<Integer> handleRecipeInner(IO io, GTRecipe recipe, List<Integer> left, boolean simulate) {
        if (io != handlerIO) return left;
        sync();
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
        if (machine.getLevel() != null) demandUntil = machine.getLevel().getGameTime() + 40;
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
        if (grouped) sync();
        tag.putInt(nbtKey, grouped ? 0 : stored);
        tag.putInt(nbtKey + "_fluid", fluidIndex);
        tag.putBoolean(nbtKey + "_passive", passive);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        stored = Math.max(0, Math.min(baseCapacity, tag.getInt(nbtKey)));
        lastSeen = 0;
        grouped = false;
        fluidIndex = Math.max(0, tag.getInt(nbtKey + "_fluid"));
        passive = !tag.contains(nbtKey + "_passive") || tag.getBoolean(nbtKey + "_passive");
    }
}
