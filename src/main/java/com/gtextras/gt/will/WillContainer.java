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
            budget = handlerIO == IO.IN ? drainItems(items, budget) : fillGems(items, budget);
        }
    }

    private int drainItems(IItemHandler items, int budget) {
        for (int slot = 0; slot < items.getSlots() && budget > 0 && getSpace() > 0; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof IDemonWillGem gem) {
                double have = gem.getWill(type, stack);
                int take = (int) Math.min(Math.min(Math.floor(have), getSpace()), budget);
                if (take > 0) {
                    double drained = gem.drainWill(type, stack, take, true);
                    int got = (int) Math.floor(drained);
                    if (got > 0) {
                        setStored(stored + got);
                        budget -= got;
                    }
                }
            } else if (stack.getItem() instanceof IDemonWill soul && soul.getType(stack) == type) {
                int worth = (int) Math.floor(soul.getWill(type, stack));
                if (worth > 0 && worth <= getSpace()) {
                    ItemStack taken = items.extractItem(slot, 1, false);
                    if (!taken.isEmpty()) {
                        setStored(stored + worth);
                        budget -= worth;
                    }
                }
            } else if (stack.getItem() instanceof IDiscreteDemonWill crystal && crystal.getType(stack) == type) {
                int worth = (int) Math.floor(crystal.getDiscretization(stack));
                if (worth > 0 && worth <= getSpace()) {
                    ItemStack taken = items.extractItem(slot, 1, false);
                    if (!taken.isEmpty()) {
                        setStored(stored + worth);
                        budget -= worth;
                    }
                }
            }
        }
        return budget;
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
