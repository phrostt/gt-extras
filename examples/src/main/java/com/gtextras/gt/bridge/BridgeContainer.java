package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableRecipeHandlerTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gtextras.gt.trait.Linkable;
import com.gtextras.gt.trait.LinkSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BridgeContainer extends NotifiableRecipeHandlerTrait<String> implements Linkable {

    public static final int INTERVAL = 10;
    public static final long MAX_PER_KEY = 10_000_000L;
    public static final String RESONANCE = "data resonance";

    private final BridgeDef def;
    private final IO io;
    private final int tier;
    private final String nbt;
    private final Map<String, Long> store = new LinkedHashMap<>();
    private final LinkSet links = new LinkSet();
    private boolean returnBeam;
    private double decayCarry;
    private boolean armed;
    private TickableSubscription tickSubscription;
    private final BridgeEnergy energy;

    public BridgeContainer(MetaMachine machine, IO io, int tier, BridgeDef def, int amps) {
        super(machine);
        this.io = io;
        this.tier = tier;
        this.def = def;
        this.nbt = "gtextras_bridge_" + io.name().toLowerCase(java.util.Locale.ROOT);
        this.energy = def.energyEnabled ? new BridgeEnergy(machine, this, io, tier, def, amps) : null;
    }

    public BridgeEnergy energy() {
        return energy;
    }

    public BridgeDef def() {
        return def;
    }

    public IO io() {
        return io;
    }

    public MetaMachine owner() {
        return machine;
    }

    public boolean returnBeam() {
        return returnBeam;
    }

    public void toggleReturnBeam() {
        if (machine.isRemote()) return;
        returnBeam = !returnBeam;
        machine.markDirty();
    }

    public void emptyBuffer() {
        if (machine.isRemote()) return;
        store.clear();
        if (energy != null) energy.clear();
        notifyListeners();
        machine.markDirty();
    }

    public long resonanceCapacity() {
        return (long) def.resonanceCap * Math.max(1, tier - GTValues.LV + 1);
    }

    public long level(String key) {
        return store.getOrDefault(key, 0L);
    }

    public double fraction(String key) {
        long cap = resonanceCapacity();
        return cap <= 0 ? 0 : Math.max(0.0, Math.min(1.0, level(key) / (double) cap));
    }

    public int entryCount() {
        return store.size();
    }

    public String entryText(int index) {
        if (index < 0 || index >= store.size()) return "";
        int i = 0;
        for (Map.Entry<String, Long> e : store.entrySet()) {
            if (i++ == index) return e.getKey() + "  x" + String.format("%,d", e.getValue());
        }
        return "";
    }

    private long capFor(String key) {
        return key.equals(RESONANCE) ? resonanceCapacity() : MAX_PER_KEY;
    }

    public long receive(String key, long amount) {
        long current = store.getOrDefault(key, 0L);
        long add = Math.max(0L, Math.min(amount, capFor(key) - current));
        if (add > 0) {
            store.put(key, current + add);
            notifyListeners();
            machine.markDirty();
        }
        return add;
    }

    public static BridgeContainer findAt(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MetaMachineBlockEntity holder)) return null;
        for (MachineTrait trait : holder.getMetaMachine().getTraits()) {
            if (trait instanceof BridgeContainer container) return container;
        }
        return null;
    }

    @Override
    public void onMachineLoad() {
        super.onMachineLoad();
        if (!machine.isRemote() && tickSubscription == null) {
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
        if (machine.getOffsetTimer() % INTERVAL != 0) return;
        Level level = machine.getLevel();
        if (level == null) return;
        if (io == IO.IN) {
            decay();
        } else {
            transfer(level);
        }
    }

    private void decay() {
        if (def.resonanceDecayPerSecond <= 0) return;
        long current = store.getOrDefault(RESONANCE, 0L);
        if (current <= 0) return;
        decayCarry += (double) def.resonanceDecayPerSecond * INTERVAL / 20.0;
        long step = (long) decayCarry;
        if (step <= 0) return;
        decayCarry -= step;
        long next = Math.max(0L, current - step);
        if (next == 0) {
            store.remove(RESONANCE);
        } else {
            store.put(RESONANCE, next);
        }
        notifyListeners();
        machine.markDirty();
    }

    private void transfer(Level level) {
        if (store.isEmpty() || links.isEmpty()) return;
        BlockPos pos = machine.getPos();
        boolean moved = false;
        for (BlockPos target : links.ordered()) {
            if (store.isEmpty()) break;
            if (target.distSqr(pos) > (double) def.range * def.range || !level.isLoaded(target)) continue;
            BridgeContainer other = findAt(level, target);
            if (other == null || other.io != IO.IN || !other.def.id.equals(def.id)) continue;
            boolean any = false;
            for (Map.Entry<String, Long> entry : new ArrayList<>(store.entrySet())) {
                long accepted = other.receive(entry.getKey(), entry.getValue());
                if (accepted <= 0) continue;
                long left = entry.getValue() - accepted;
                if (left <= 0) {
                    store.remove(entry.getKey());
                } else {
                    store.put(entry.getKey(), left);
                }
                any = true;
            }
            if (any) {
                moved = true;
                links.advance();
                BridgeFx.line(level, pos, target, def.rgb(returnBeam), 0.7f, 1.5);
            }
        }
        if (moved) {
            notifyListeners();
            machine.markDirty();
        }
    }

    private static boolean inTag(String tagName, String itemName) {
        ResourceLocation tagId = ResourceLocation.tryParse(tagName);
        ResourceLocation itemId = ResourceLocation.tryParse(itemName);
        if (tagId == null || itemId == null) return false;
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) return false;
        return item.builtInRegistryHolder().is(TagKey.create(Registries.ITEM, tagId));
    }

    private long take(Map<String, Long> from, BridgePacket packet) {
        long wanted = packet.amount();
        if (packet.kind().equals(BridgePacket.TAG)) {
            for (Map.Entry<String, Long> entry : new ArrayList<>(from.entrySet())) {
                if (wanted <= 0) break;
                String[] parts = entry.getKey().split(" ", 2);
                if (parts.length != 2 || !parts[0].equals(BridgePacket.ITEM) || !inTag(packet.name(), parts[1])) {
                    continue;
                }
                long used = Math.min(wanted, entry.getValue());
                wanted -= used;
                from.put(entry.getKey(), entry.getValue() - used);
            }
            return wanted;
        }
        long have = from.getOrDefault(packet.key(), 0L);
        long used = Math.min(wanted, have);
        if (used > 0) from.put(packet.key(), have - used);
        return wanted - used;
    }

    private long give(Map<String, Long> to, BridgePacket packet) {
        long have = to.getOrDefault(packet.key(), 0L);
        long add = Math.max(0L, Math.min(packet.amount(), capFor(packet.key()) - have));
        if (add > 0) to.put(packet.key(), have + add);
        return packet.amount() - add;
    }

    @Override
    public List<String> handleRecipeInner(IO ioMode, GTRecipe recipe, List<String> left, boolean simulate) {
        if (ioMode != io) return left;
        Map<String, Long> work = simulate ? new LinkedHashMap<>(store) : store;
        List<String> rest = new ArrayList<>();
        boolean changed = false;
        for (String text : left) {
            BridgePacket packet;
            try {
                packet = BridgePacket.parse(text);
            } catch (IllegalArgumentException e) {
                rest.add(text);
                continue;
            }
            if (!packet.channel().equals(def.id)) {
                rest.add(text);
                continue;
            }
            long remaining;
            if (io == IO.IN) {
                remaining = take(work, packet);
            } else {
                long overflow = give(work, packet);
                remaining = 0L;
                if (overflow != packet.amount()) changed = true;
            }
            if (io == IO.IN && remaining != packet.amount()) changed = true;
            if (remaining > 0) rest.add(packet.withAmount(remaining).toString());
        }
        if (!simulate) {
            work.values().removeIf(v -> v <= 0);
            if (changed) {
                notifyListeners();
                machine.markDirty();
            }
        }
        return rest.isEmpty() ? null : rest;
    }

    @Override
    public List<Object> getContents() {
        List<Object> contents = new ArrayList<>();
        if (io != IO.IN) return contents;
        for (Map.Entry<String, Long> e : store.entrySet()) {
            contents.add(def.id + " " + e.getKey() + " " + e.getValue());
        }
        return contents;
    }

    @Override
    public double getTotalContentAmount() {
        double total = 0;
        for (long v : store.values()) total += v;
        return total;
    }

    @Override
    public int getSize() {
        return io == IO.IN ? store.size() : 0;
    }

    @Override
    public RecipeCapability<String> getCapability() {
        return BridgeCapability.CAP;
    }

    @Override
    public IO getHandlerIO() {
        return io;
    }

    @Override
    public LinkSet links() {
        return links;
    }

    @Override
    public boolean canLink(Level level, BlockPos target) {
        BridgeContainer other = findAt(level, target);
        return other != null && other != this && other.io == IO.IN && other.def.id.equals(def.id);
    }

    @Override
    public int linkRange() {
        return def.maxLinkRange();
    }

    @Override
    public int[] linkColor() {
        return def.rgb(returnBeam);
    }

    @Override
    public boolean linkable() {
        return io == IO.OUT;
    }

    @Override
    public BlockPos linkOrigin() {
        return machine.getPos();
    }

    @Override
    public void markLinksDirty() {
        machine.markDirty();
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        ListTag list = new ListTag();
        for (Map.Entry<String, Long> e : store.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.putString("k", e.getKey());
            c.putLong("v", e.getValue());
            list.add(c);
        }
        tag.put(nbt + "_store", list);
        tag.putBoolean(nbt + "_return", returnBeam);
        links.save(tag, nbt + "_links");
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        store.clear();
        ListTag list = tag.getList(nbt + "_store", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            long v = c.getLong("v");
            if (v > 0) store.put(c.getString("k"), v);
        }
        returnBeam = tag.getBoolean(nbt + "_return");
        links.load(tag, nbt + "_links");
    }
}
