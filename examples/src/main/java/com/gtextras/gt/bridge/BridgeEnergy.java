package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

public class BridgeEnergy extends MachineTrait implements IEnergyContainer {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(BridgeEnergy.class);
    private static final long UNLIMITED_AMPS = 1_000_000L;
    private static final long SIZING_AMPS = 1024L;

    private final BridgeContainer container;
    private final BridgeDef def;
    private final IO io;
    private final long voltage;
    private final int tier;
    private final long amperage;
    private final long capacity;
    private final String nbt;
    private long stored;
    private long amps;
    private long lastTick = Long.MIN_VALUE;
    private long perSec;
    private long perSecAccumulator;
    private TickableSubscription subscription;

    public BridgeEnergy(MetaMachine machine, BridgeContainer container, IO io, int tier, BridgeDef def, int amps) {
        super(machine);
        this.container = container;
        this.def = def;
        this.io = io;
        this.voltage = GTValues.V[tier];
        this.tier = tier;
        long limit = amps > 0 ? amps : def.energyMaxAmperage;
        this.amperage = limit > 0 ? limit : UNLIMITED_AMPS;
        long sizing = limit > 0 ? limit : SIZING_AMPS;
        long cap = def.energyBufferSize > 0 ? def.energyBufferSize : voltage * sizing * def.energyBufferTicks;
        this.capacity = cap <= 0 || cap > Long.MAX_VALUE / 4 ? Long.MAX_VALUE / 4 : cap;
        this.nbt = "gtextras_bridge_energy_" + io.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (!machine.isRemote() && subscription == null) {
            subscription = machine.subscribeServerTick(this::tick);
        }
    }

    @Override
    public void onMachineUnLoad() {
        super.onMachineUnLoad();
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    public long room() {
        return capacity - stored;
    }

    public long stored() {
        return stored;
    }

    public long capacity() {
        return capacity;
    }

    public long perSecond() {
        return perSec;
    }

    public void clear() {
        stored = 0;
        machine.markDirty();
    }

    private void tick() {
        Level level = machine.getLevel();
        if (level == null) return;
        long time = machine.getOffsetTimer();
        if (time % 20 == 0) {
            perSec = perSecAccumulator;
            perSecAccumulator = 0;
        }
        if (stored <= 0) return;
        if (io == IO.IN) {
            push(level);
        } else if (time % BridgeContainer.INTERVAL == 0) {
            send(level);
        }
    }

    private void push(Level level) {
        if (stored < voltage) return;
        long wanted = Math.min(stored / voltage, amperage);
        long used = 0;
        BlockPos pos = machine.getPos();
        for (Direction side : GTUtil.DIRECTIONS) {
            if (used >= wanted) break;
            Direction opposite = side.getOpposite();
            IEnergyContainer target = GTCapabilityHelper.getEnergyContainer(level, pos.relative(side), opposite);
            if (target != null && target.inputsEnergy(opposite)) {
                used += target.acceptEnergyFromNetwork(opposite, voltage, wanted - used);
            }
        }
        if (used > 0) {
            long taken = used * voltage;
            stored -= taken;
            perSecAccumulator += taken;
            machine.markDirty();
        }
    }

    private void send(Level level) {
        if (container.links().isEmpty()) return;
        BlockPos pos = machine.getPos();
        double reach = (double) def.energyRange * def.energyRange;
        boolean moved = false;
        for (BlockPos target : container.links().ordered()) {
            if (stored <= 0) break;
            if (target.distSqr(pos) > reach || !level.isLoaded(target)) continue;
            BridgeContainer other = BridgeContainer.findAt(level, target);
            if (other == null || other.io() != IO.IN || !other.def().id.equals(def.id) || other.energy() == null) {
                continue;
            }
            BridgeEnergy sink = other.energy();
            if (!def.energyAllowTierMismatch && sink.tier != tier) continue;
            long keep = 100 - def.energyLossPercent;
            long sendable = Math.min(stored, keep >= 100 ? sink.room() : sink.room() * 100 / keep);
            if (sendable <= 0) continue;
            long received = keep >= 100 ? sendable : sendable * keep / 100;
            if (received <= 0) continue;
            stored -= sendable;
            sink.stored += received;
            sink.machine.markDirty();
            perSecAccumulator += sendable;
            moved = true;
            container.links().advance();
            BridgeFx.line(level, pos, target, def.rgb(false), 0.5f, 1.0);
        }
        if (moved) machine.markDirty();
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long volts, long amperes) {
        if (io != IO.OUT || volts <= 0 || volts > voltage) return 0;
        long time = machine.getOffsetTimer();
        if (lastTick != time) {
            lastTick = time;
            amps = 0;
        }
        if (amps >= amperage) return 0;
        long accepted = Math.min(Math.min(amperes, amperage - amps), room() / volts);
        if (accepted <= 0) return 0;
        stored += accepted * volts;
        amps += accepted;
        perSecAccumulator += accepted * volts;
        machine.markDirty();
        return accepted;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return io == IO.OUT;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return io == IO.IN;
    }

    @Override
    public long changeEnergy(long difference) {
        long before = stored;
        stored = Math.max(0L, Math.min(capacity, stored + difference));
        return stored - before;
    }

    @Override
    public long getEnergyStored() {
        return stored;
    }

    @Override
    public long getEnergyCapacity() {
        return capacity;
    }

    @Override
    public long getInputAmperage() {
        return io == IO.OUT ? amperage : 0L;
    }

    @Override
    public long getInputVoltage() {
        return io == IO.OUT ? voltage : 0L;
    }

    @Override
    public long getOutputAmperage() {
        return io == IO.IN ? amperage : 0L;
    }

    @Override
    public long getOutputVoltage() {
        return io == IO.IN ? voltage : 0L;
    }

    @Override
    public long getInputPerSec() {
        return io == IO.OUT ? perSec : 0L;
    }

    @Override
    public long getOutputPerSec() {
        return io == IO.IN ? perSec : 0L;
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putLong(nbt, stored);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        stored = Math.max(0L, Math.min(capacity, tag.getLong(nbt)));
    }
}
