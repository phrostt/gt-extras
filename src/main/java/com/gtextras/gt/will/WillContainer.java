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

    private boolean gemMode;
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
    }

    public boolean gemMode() {
        return gemMode;
    }

    public void setGemMode(boolean value) {
        gemMode = value;
        machine.markDirty();
    }

    @Override
    public void saveCustomPersistedData(net.minecraft.nbt.CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        tag.putBoolean(nbtKey + "_gems", gemMode);
    }

    @Override
    public void loadCustomPersistedData(net.minecraft.nbt.CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        gemMode = tag.getBoolean(nbtKey + "_gems");
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
        if (handlerIO == IO.IN) {
            int moved = ExtrasFluids.pull(level, pos, fluidList(), Math.min(getSpace(), transferPerOperation));
            if (moved > 0) setStored(stored + moved);
            return;
        } else if (!gemMode) {
            int moved = ExtrasFluids.push(level, pos, fluidList(), Math.min(stored, transferPerOperation), 0);
            if (moved > 0) setStored(stored - moved);
        }
        if (handlerIO == IO.OUT && !gemMode) return;
        int budget = transferPerOperation;
        for (Direction dir : Direction.values()) {
            if (budget <= 0) break;
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be == null || be instanceof IMachineBlockEntity) continue;
            IItemHandler items = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).resolve()
                    .orElse(null);
            if (items == null) continue;
            budget = fillGems(items, budget);
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
        if (wanted > stored && wanted <= Integer.MAX_VALUE) {
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
        Level level = machine.getLevel();
        if (level == null || need <= 0) return 0;
        BlockPos pos = machine.getPos();
        int got = 0;
        for (Direction dir : Direction.values()) {
            if (got >= need) break;
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be == null || be instanceof IMachineBlockEntity) continue;
            IItemHandler items = be.getCapability(ForgeCapabilities.ITEM_HANDLER, dir.getOpposite()).resolve()
                    .orElse(null);
            if (items == null) continue;
            got += pullFrom(items, need - got, need, simulate, got);
        }
        return got;
    }

    private int pullFrom(IItemHandler items, int remaining, int need, boolean simulate, int alreadyGot) {
        int got = 0;
        for (int slot = 0; slot < items.getSlots() && got < remaining; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof IDemonWillGem gem) {
                int have = (int) Math.floor(gem.getWill(type, stack));
                int take = Math.min(have, remaining - got);
                if (take <= 0) continue;
                if (simulate) {
                    got += take;
                } else {
                    got += (int) Math.floor(gem.drainWill(type, stack, take, true));
                }
                continue;
            }
            int worth = discreteWorth(stack);
            if (worth <= 0) continue;
            int count = stack.getCount();
            while (count > 0 && got < remaining) {
                if (alreadyGot + got + worth - need > capacity) break;
                if (!simulate && items.extractItem(slot, 1, false).isEmpty()) break;
                got += worth;
                count--;
            }
        }
        return got;
    }

    private int discreteWorth(ItemStack stack) {
        if (stack.getItem() instanceof IDemonWill soul && soul.getType(stack) == type) {
            return (int) Math.floor(soul.getWill(type, stack));
        }
        int extra = matchExtra(stack);
        if (extra > 0) return extra;
        if (stack.getItem() instanceof IDiscreteDemonWill crystal && crystal.getType(stack) == type) {
            return (int) Math.floor(crystal.getDiscretization(stack));
        }
        return 0;
    }

    private int matchExtra(ItemStack stack) {
        for (ExtrasFluids.WillItem entry : ExtrasFluids.willItems()) {
            if (entry.type() != typeIndex) continue;
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
