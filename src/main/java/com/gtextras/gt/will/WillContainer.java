package com.gtextras.gt.will;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.gt.ExtrasCapabilities;
import com.gtextras.gt.trait.AmountContainer;
import com.gtextras.gt.trait.ExtrasFluids;
import com.gtextras.gt.trait.HatchConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import wayoftime.bloodmagic.api.compat.EnumDemonWillType;
import wayoftime.bloodmagic.api.compat.IDemonWill;
import wayoftime.bloodmagic.api.compat.IDemonWillGem;
import wayoftime.bloodmagic.api.compat.IDiscreteDemonWill;

import java.util.List;

public class WillContainer extends AmountContainer {

    public static final int INTERVAL = 10;
    private static final EnumDemonWillType[] TYPES = { EnumDemonWillType.DEFAULT, EnumDemonWillType.CORROSIVE,
            EnumDemonWillType.DESTRUCTIVE, EnumDemonWillType.VENGEFUL, EnumDemonWillType.STEADFAST };

    private boolean outputActive;
    private WillContainer rawSource;

    public void setRawSource(WillContainer raw) {
        rawSource = raw;
    }
    private final int typeIndex;
    private final EnumDemonWillType type;

    public static int transferFor(int tier) {
        return Math.max(1, HatchConfig.rate("will", tier) * INTERVAL);
    }

    public static int capacityFor(int tier) {
        return HatchConfig.storage("will", tier);
    }

    public WillContainer(MetaMachine machine, IO io, int tier, int typeIndex) {
        super(machine, io, ExtrasCapabilities.WILL[typeIndex], capacityFor(tier), transferFor(tier), INTERVAL,
                "gtextras_will_" + ExtrasCapabilities.WILL_NAMES[typeIndex]);
        this.typeIndex = typeIndex;
        this.type = TYPES[typeIndex];
        this.outputActive = typeIndex == 0;
        configureCost("will", tier);
    }

    public boolean outputActive() {
        return outputActive;
    }

    public void setOutputActive(boolean value) {
        outputActive = value;
        machine.markDirty();
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putBoolean(nbtKey + "_out", outputActive);
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        outputActive = tag.contains(nbtKey + "_out") ? tag.getBoolean(nbtKey + "_out") : typeIndex == 0;
    }

    public int typeIndex() {
        return typeIndex;
    }

    @Override
    protected List<ExtrasFluids.Entry> fluidList() {
        return ExtrasFluids.will(typeIndex);
    }

    @Override
    protected void transferWithNeighbours() {
        Level level = machine.getLevel();
        if (level == null) return;
        BlockPos pos = machine.getPos();
        int budget = transferPerOperation;
        if (handlerIO == IO.IN && typeIndex != 0) return;
        if (typeIndex == 0) {
            int space = Math.min(getSpace(), budget);
            for (int i = 0; i < ExtrasCapabilities.WILL_NAMES.length && space > 0; i++) {
                int moved = ExtrasFluids.pull(level, pos, ExtrasFluids.will(i), space);
                if (moved > 0) {
                    setStored(stored + moved);
                    space -= moved;
                }
            }
            if (space > 0) {
                int got = pullFromItems(space, false, null, getSpace());
                if (got > 0) setStored(stored + got);
            }
        }
        if (handlerIO == IO.IN) return;
        if (outputActive && typeIndex != 0 && rawSource != null && stored < capacity) {
            int draw = Math.min(Math.min(rawSource.getStored(), getSpace()), budget);
            if (draw > 0) {
                rawSource.setStored(rawSource.getStored() - draw);
                setStored(stored + draw);
            }
        }
        if (!outputActive || stored <= 0) return;
        int moved = ExtrasFluids.push(level, pos, fluidList(), Math.min(stored, budget), 0);
        if (moved > 0) {
            setStored(stored - moved);
            budget -= moved;
        }
        for (Direction dir : Direction.values()) {
            if (stored <= 0) break;
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be == null || be instanceof IMachineBlockEntity) continue;
            IItemHandler items = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).resolve()
                    .orElse(null);
            if (items == null) continue;
            budget = fillGems(items, budget);
            spitGems(items);
        }
    }

    private static final int GEM_WORTH = 144;

    private void spitGems(IItemHandler items) {
        for (int slot = 0; slot < items.getSlots(); slot++) {
            if (items.getStackInSlot(slot).getItem() instanceof IDemonWillGem) return;
        }
        net.minecraft.world.item.Item gem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation("gtceu",
                        (typeIndex == 0 ? "demon" : ExtrasCapabilities.WILL_NAMES[typeIndex]) + "_gem"));
        if (gem == null || gem == net.minecraft.world.item.Items.AIR) return;
        int spat = 0;
        while (stored >= GEM_WORTH && spat < 8) {
            ItemStack left = net.minecraftforge.items.ItemHandlerHelper.insertItem(items, new ItemStack(gem), false);
            if (!left.isEmpty()) break;
            setStored(stored - GEM_WORTH);
            spat++;
        }
    }

    @Override
    public java.util.List<Integer> handleRecipeInner(IO io, com.gregtechceu.gtceu.api.recipe.GTRecipe recipe,
                                                     java.util.List<Integer> left, boolean simulate) {
        if (io != IO.IN || handlerIO != IO.IN) {
            return super.handleRecipeInner(io, recipe, left, simulate);
        }
        long wanted = 0;
        for (Integer value : left) {
            if (value != null) wanted += value;
        }
        if (typeIndex == 0 && wanted > stored && wanted <= Integer.MAX_VALUE) {
            int need = (int) (wanted - stored);
            int got = pullFromItems(need, simulate);
            if (!simulate && got > 0) {
                int before = stored;
                setStored(stored + got);
                com.gtextras.GTExtras.LOGGER.info("[gtextras] will {}: pulled {} from inventories at {} for recipe {}, buffer {} -> {}",
                        nbtKey, got, machine.getPos(), recipe.id, before, stored);
            } else if (simulate && got > 0) {
                java.util.List<Integer> rest = new java.util.ArrayList<>(1);
                long remaining = Math.max(0L, wanted - stored - got);
                if (remaining <= 0) return null;
                rest.add((int) Math.min(remaining, Integer.MAX_VALUE));
                return rest;
            }
        }
        return super.handleRecipeInner(io, recipe, left, simulate);
    }

    private int pullFromItems(int need, boolean simulate) {
        return pullFromItems(need, simulate, null, need + capacity);
    }

    private int pullFromItems(int need, boolean simulate, Direction skip, int limit) {
        Level level = machine.getLevel();
        if (level == null || need <= 0) return 0;
        BlockPos pos = machine.getPos();
        int got = 0;
        for (Direction dir : Direction.values()) {
            if (got >= need) break;
            if (dir == skip) continue;
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be == null || be instanceof IMachineBlockEntity) continue;
            IItemHandler items = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).resolve()
                    .orElse(null);
            if (items == null) continue;
            got += pullFrom(items, need - got, limit, simulate, got);
        }
        return got;
    }

    private int pullFrom(IItemHandler items, int remaining, int limit, boolean simulate, int alreadyGot) {
        int got = 0;
        for (int slot = 0; slot < items.getSlots() && got < remaining; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof IDemonWillGem gem) {
                for (EnumDemonWillType kind : TYPES) {
                    if (got >= remaining) break;
                    int have = (int) Math.floor(gem.getWill(kind, stack));
                    int take = Math.min(have, remaining - got);
                    if (take <= 0) continue;
                    if (simulate) {
                        got += take;
                    } else {
                        got += (int) Math.floor(gem.drainWill(kind, stack, take, true));
                    }
                }
                continue;
            }
            int worth = discreteWorth(stack);
            if (worth <= 0) continue;
            int count = stack.getCount();
            while (count > 0 && got < remaining) {
                if (alreadyGot + got + worth > limit) break;
                if (!simulate && items.extractItem(slot, 1, false).isEmpty()) break;
                got += worth;
                count--;
            }
        }
        return got;
    }

    private int discreteWorth(ItemStack stack) {
        if (stack.getItem() instanceof IDemonWill soul) {
            return (int) Math.floor(soul.getWill(soul.getType(stack), stack));
        }
        int extra = matchExtra(stack);
        if (extra > 0) return extra;
        if (stack.getItem() instanceof IDiscreteDemonWill crystal) {
            return (int) Math.floor(crystal.getDiscretization(stack));
        }
        return materialWorth(stack);
    }

    private static final java.util.Set<String> WILL_MATERIALS = java.util.Set.of("demon", "corrosive",
            "destructive", "vengeful", "steadfast");

    private int materialWorth(ItemStack stack) {
        try {
            com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack info =
                    com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper.getMaterialStack(stack);
            if (info == null || info.material() == null || info.amount() <= 0) return 0;
            if (!WILL_MATERIALS.contains(info.material().getName())) return 0;
            return (int) Math.min(Integer.MAX_VALUE, info.amount() * 144L
                    / com.gregtechceu.gtceu.api.GTValues.M);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private int matchExtra(ItemStack stack) {
        for (ExtrasFluids.WillItem entry : ExtrasFluids.willItems()) {
            net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(entry.key());
            if (id == null) continue;
            if (entry.tag()) {
                if (stack.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, id))) {
                    return entry.amount();
                }
            } else if (id.equals(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem()))) {
                return entry.amount();
            }
        }
        return 0;
    }

    private int fillGems(IItemHandler items, int budget) {
        for (int slot = 0; slot < items.getSlots() && budget > 0 && stored > 0; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty() || !(stack.getItem() instanceof IDemonWillGem gem)) continue;
            int give = Math.min(stored, budget);
            double filled = gem.fillWill(type, stack, give, true);
            int used = (int) Math.floor(filled);
            if (used > 0) {
                setStored(stored - used);
                budget -= used;
            }
        }
        return budget;
    }
}
