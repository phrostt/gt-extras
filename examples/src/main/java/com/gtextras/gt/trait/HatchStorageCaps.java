package com.gtextras.gt.trait;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.gtextras.GTExtras;
import com.gtextras.gt.blood.BloodContainer;
import com.gtextras.gt.blood.BloodHatchMachine;
import com.gtextras.gt.will.WillContainer;
import com.gtextras.gt.will.WillHatchMachine;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public final class HatchStorageCaps {

    private static final ResourceLocation ID = new ResourceLocation(GTExtras.MOD_ID, "hatch_storage");

    private HatchStorageCaps() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, HatchStorageCaps::attach);
    }

    private static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        if (event.getObject() instanceof MetaMachineBlockEntity be) {
            event.addCapability(ID, new Provider(be));
        }
    }

    private static boolean server(AmountContainer container) {
        Level level = container.owner().getLevel();
        return level != null && !level.isClientSide;
    }

    private static ExtrasFluids.Entry find(List<ExtrasFluids.Entry> list, net.minecraft.world.level.material.Fluid fluid) {
        for (ExtrasFluids.Entry entry : list) {
            if (entry.fluid() == fluid) return entry;
        }
        return null;
    }

    private static final class Provider implements ICapabilityProvider {

        private final MetaMachineBlockEntity be;
        private LazyOptional<IFluidHandler> fluid;
        private LazyOptional<IItemHandler> items;

        private Provider(MetaMachineBlockEntity be) {
            this.be = be;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap != ForgeCapabilities.FLUID_HANDLER && cap != ForgeCapabilities.ITEM_HANDLER) {
                return LazyOptional.empty();
            }
            MetaMachine machine = be.getMetaMachine();
            if (machine instanceof BloodHatchMachine blood) {
                if (cap == ForgeCapabilities.FLUID_HANDLER) {
                    if (fluid == null) {
                        IFluidHandler handler = new BloodFluid(blood.container());
                        fluid = LazyOptional.of(() -> handler);
                    }
                    return fluid.cast();
                }
                if (items == null) {
                    IItemHandler handler = new BloodItems(blood.container());
                    items = LazyOptional.of(() -> handler);
                }
                return items.cast();
            }
            if (machine instanceof WillHatchMachine will) {
                if (cap == ForgeCapabilities.FLUID_HANDLER) {
                    if (fluid == null) {
                        IFluidHandler handler = new WillFluid(will.containers(), will.io());
                        fluid = LazyOptional.of(() -> handler);
                    }
                    return fluid.cast();
                }
                if (items == null) {
                    IItemHandler handler = new WillItems(will.containers(), will.io());
                    items = LazyOptional.of(() -> handler);
                }
                return items.cast();
            }
            return LazyOptional.empty();
        }
    }

    private static final class BloodFluid implements IFluidHandler {

        private final BloodContainer c;

        private BloodFluid(BloodContainer c) {
            this.c = c;
        }

        private ExtrasFluids.Entry selected() {
            List<ExtrasFluids.Entry> list = ExtrasFluids.blood();
            if (list.isEmpty()) return null;
            return list.get(Math.floorMod(c.fluidIndex(), list.size()));
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            ExtrasFluids.Entry e = selected();
            if (e == null || c.getStored() <= 0) return FluidStack.EMPTY;
            return new FluidStack(e.fluid(), (int) (c.getStored() / e.ratio()));
        }

        @Override
        public int getTankCapacity(int tank) {
            ExtrasFluids.Entry e = selected();
            return e == null ? 0 : (int) (c.getCapacity() / e.ratio());
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return c.io() == IO.IN && find(ExtrasFluids.blood(), stack.getFluid()) != null;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (c.io() != IO.IN || resource.isEmpty()) return 0;
            ExtrasFluids.Entry e = find(ExtrasFluids.blood(), resource.getFluid());
            if (e == null) return 0;
            int mb = (int) Math.min(resource.getAmount(), (long) (c.getSpace() / e.ratio()));
            if (mb <= 0) return 0;
            if (action.execute() && server(c)) c.setStored(c.getStored() + (int) Math.round(mb * e.ratio()));
            return mb;
        }

        private FluidStack take(ExtrasFluids.Entry e, int max, FluidAction action) {
            if (c.io() != IO.OUT || e == null) return FluidStack.EMPTY;
            int mb = (int) Math.min(max, (long) (c.getStored() / e.ratio()));
            if (mb <= 0) return FluidStack.EMPTY;
            if (action.execute() && server(c)) c.setStored(c.getStored() - (int) Math.round(mb * e.ratio()));
            return new FluidStack(e.fluid(), mb);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            ExtrasFluids.Entry e = selected();
            if (resource.isEmpty() || e == null || e.fluid() != resource.getFluid()) return FluidStack.EMPTY;
            return take(e, resource.getAmount(), action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return take(selected(), maxDrain, action);
        }
    }

    private static final class BloodItems implements IItemHandler {

        private final BloodContainer c;

        private BloodItems(BloodContainer c) {
            this.c = c;
        }

        private int worth(ItemStack stack) {
            return ExtrasFluids.itemWorth(stack, ExtrasFluids.blood(), ExtrasFluids.bloodItems());
        }

        @Override
        public int getSlots() {
            return c.io() == IO.IN ? 1 : 0;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (c.io() != IO.IN || stack.isEmpty()) return stack;
            int worth = worth(stack);
            if (worth <= 0) return stack;
            int n = Math.min(stack.getCount(), c.getSpace() / worth);
            if (n <= 0) return stack;
            if (!simulate && server(c)) c.setStored(c.getStored() + n * worth);
            return stack.copyWithCount(stack.getCount() - n);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return c.io() == IO.IN && worth(stack) > 0;
        }
    }

    private static int will(WillContainer[] cs, int type) {
        WillContainer c = cs[type];
        if (!c.outputActive()) return 0;
        long total = c.getStored();
        if (type != 0) total += cs[0].getStored();
        return (int) Math.min(Integer.MAX_VALUE, total);
    }

    private static int drawWill(WillContainer[] cs, int type, int units) {
        WillContainer c = cs[type];
        int own = Math.min(units, c.getStored());
        if (own > 0) c.setStored(c.getStored() - own);
        int rest = units - own;
        if (rest > 0 && type != 0) {
            int raw = Math.min(rest, cs[0].getStored());
            if (raw > 0) {
                cs[0].setStored(cs[0].getStored() - raw);
                own += raw;
            }
        }
        return own;
    }

    private static final class WillFluid implements IFluidHandler {

        private final WillContainer[] cs;
        private final IO io;

        private WillFluid(WillContainer[] cs, IO io) {
            this.cs = cs;
            this.io = io;
        }

        private ExtrasFluids.Entry first(int type) {
            List<ExtrasFluids.Entry> list = ExtrasFluids.will(type);
            return list.isEmpty() ? null : list.get(0);
        }

        @Override
        public int getTanks() {
            return io == IO.IN ? 1 : cs.length;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            int type = io == IO.IN ? 0 : tank;
            ExtrasFluids.Entry e = first(type);
            int units = io == IO.IN ? cs[0].getStored() : will(cs, type);
            if (e == null || units <= 0) return FluidStack.EMPTY;
            return new FluidStack(e.fluid(), (int) (units / e.ratio()));
        }

        @Override
        public int getTankCapacity(int tank) {
            ExtrasFluids.Entry e = first(io == IO.IN ? 0 : tank);
            return e == null ? 0 : (int) (cs[0].getCapacity() / e.ratio());
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return io == IO.IN && find(ExtrasFluids.will(0), stack.getFluid()) != null;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (io != IO.IN || resource.isEmpty()) return 0;
            ExtrasFluids.Entry e = find(ExtrasFluids.will(0), resource.getFluid());
            if (e == null) return 0;
            int mb = (int) Math.min(resource.getAmount(), (long) (cs[0].getSpace() / e.ratio()));
            if (mb <= 0) return 0;
            if (action.execute() && server(cs[0])) {
                cs[0].setStored(cs[0].getStored() + (int) Math.round(mb * e.ratio()));
            }
            return mb;
        }

        private FluidStack take(int type, ExtrasFluids.Entry e, int max, FluidAction action) {
            if (io != IO.OUT || e == null) return FluidStack.EMPTY;
            int mb = (int) Math.min(max, (long) (will(cs, type) / e.ratio()));
            if (mb <= 0) return FluidStack.EMPTY;
            if (action.execute() && server(cs[0])) drawWill(cs, type, (int) Math.round(mb * e.ratio()));
            return new FluidStack(e.fluid(), mb);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (io != IO.OUT || resource.isEmpty()) return FluidStack.EMPTY;
            for (int type = 0; type < cs.length; type++) {
                if (!cs[type].outputActive()) continue;
                ExtrasFluids.Entry e = find(ExtrasFluids.will(type), resource.getFluid());
                if (e != null) return take(type, e, resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (io != IO.OUT) return FluidStack.EMPTY;
            for (int type = 0; type < cs.length; type++) {
                if (cs[type].outputActive()) return take(type, first(type), maxDrain, action);
            }
            return FluidStack.EMPTY;
        }
    }

    private static final class WillItems implements IItemHandler {

        private final WillContainer[] cs;
        private final IO io;

        private WillItems(WillContainer[] cs, IO io) {
            this.cs = cs;
            this.io = io;
        }

        @Override
        public int getSlots() {
            return io == IO.IN ? 1 : cs.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (io != IO.OUT || slot < 0 || slot >= cs.length) return ItemStack.EMPTY;
            int n = Math.min(64, will(cs, slot) / WillContainer.GEM_WORTH);
            if (n <= 0) return ItemStack.EMPTY;
            ItemStack gem = cs[slot].gem();
            return gem.isEmpty() ? ItemStack.EMPTY : gem.copyWithCount(n);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (io != IO.IN || stack.isEmpty()) return stack;
            int worth = cs[0].itemWorth(stack);
            if (worth <= 0) return stack;
            int n = Math.min(stack.getCount(), cs[0].getSpace() / worth);
            if (n <= 0) return stack;
            if (!simulate && server(cs[0])) cs[0].setStored(cs[0].getStored() + n * worth);
            return stack.copyWithCount(stack.getCount() - n);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (io != IO.OUT || slot < 0 || slot >= cs.length || amount <= 0) return ItemStack.EMPTY;
            ItemStack gem = cs[slot].gem();
            if (gem.isEmpty()) return ItemStack.EMPTY;
            int n = Math.min(Math.min(amount, 64), will(cs, slot) / WillContainer.GEM_WORTH);
            if (n <= 0) return ItemStack.EMPTY;
            if (!simulate && server(cs[0])) drawWill(cs, slot, n * WillContainer.GEM_WORTH);
            return gem.copyWithCount(n);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return io == IO.IN && cs[0].itemWorth(stack) > 0;
        }
    }
}
