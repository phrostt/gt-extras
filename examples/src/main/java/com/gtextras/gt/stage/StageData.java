package com.gtextras.gt.stage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public final class StageData {

    public static final String KEY = "gtx_stages";

    private StageData() {
    }

    public static boolean has(CompoundTag data) {
        return data != null && data.contains(KEY, Tag.TAG_LIST) && !data.getList(KEY, Tag.TAG_COMPOUND).isEmpty();
    }

    public static ListTag list(CompoundTag data) {
        return data.getList(KEY, Tag.TAG_COMPOUND);
    }

    public static CompoundTag stage(double percent, List<String> success, boolean repeat, List<String> fail) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("chance", Math.max(0, Math.min(100, percent)));
        tag.putBoolean("repeat", repeat);
        if (!success.isEmpty()) {
            tag.put("success", strings(success));
        }
        if (!fail.isEmpty()) {
            tag.put("fail", strings(fail));
        }
        return tag;
    }

    private static ListTag strings(List<String> values) {
        ListTag list = new ListTag();
        for (String v : values) {
            list.add(net.minecraft.nbt.StringTag.valueOf(v.trim()));
        }
        return list;
    }

    public static List<String> specs(CompoundTag stage, String key) {
        List<String> out = new ArrayList<>();
        if (stage.contains(key, Tag.TAG_LIST)) {
            ListTag list = stage.getList(key, Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                out.add(list.getString(i));
            }
        } else if (stage.contains(key, Tag.TAG_STRING)) {
            out.add(stage.getString(key));
        }
        return out;
    }

    private static final java.util.regex.Pattern FLUID = java.util.regex.Pattern.compile("^\\S+\\s+\\d+$");
    private static final java.util.regex.Pattern ITEM_COUNT = java.util.regex.Pattern.compile("^(\\d+)x\\s+(\\S+)$");

    public static boolean isFluid(String spec) {
        return FLUID.matcher(spec.trim()).matches();
    }

    public static String id(String spec) {
        String s = spec.trim();
        java.util.regex.Matcher m = ITEM_COUNT.matcher(s);
        if (m.matches()) {
            return m.group(2);
        }
        return s.split("\\s+")[0];
    }

    public static int amount(String spec) {
        String s = spec.trim();
        try {
            java.util.regex.Matcher m = ITEM_COUNT.matcher(s);
            if (m.matches()) {
                return Math.max(1, Integer.parseInt(m.group(1)));
            }
            if (FLUID.matcher(s).matches()) {
                return Math.max(1, Integer.parseInt(s.split("\\s+")[1]));
            }
        } catch (NumberFormatException e) {
            return 1;
        }
        return 1;
    }

    public static ItemStack item(String spec, int multiplier) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id(spec)));
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, amount(spec) * multiplier);
    }

    public static FluidStack fluid(String spec, int multiplier) {
        net.minecraft.world.level.material.Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id(spec)));
        if (fluid == null || fluid == net.minecraft.world.level.material.Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(fluid, amount(spec) * multiplier);
    }

    public static List<double[]> cumulative(ListTag stages) {
        List<double[]> out = new ArrayList<>();
        double reach = 1.0;
        for (int i = 0; i < stages.size(); i++) {
            CompoundTag st = stages.getCompound(i);
            double p = st.getDouble("chance") / 100.0;
            boolean repeat = st.getBoolean("repeat");
            double success = repeat ? reach : reach * p;
            double fail = repeat ? 0 : reach * (1 - p);
            out.add(new double[]{reach, success, fail});
            reach = success;
        }
        return out;
    }
}
