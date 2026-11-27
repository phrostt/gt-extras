package com.gtextras.gt.trait;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public final class LinkSet {

    public static final int MAX = 10;

    private final List<BlockPos> links = new ArrayList<>();
    private boolean roundRobin;
    private int cursor;

    public boolean roundRobin() {
        return roundRobin;
    }

    public void setRoundRobin(boolean value) {
        roundRobin = value;
        cursor = 0;
    }

    public List<BlockPos> ordered() {
        if (!roundRobin || links.size() < 2) return links;
        int start = Math.floorMod(cursor, links.size());
        List<BlockPos> out = new ArrayList<>(links.size());
        for (int i = 0; i < links.size(); i++) out.add(links.get((start + i) % links.size()));
        return out;
    }

    public void advance() {
        if (roundRobin && !links.isEmpty()) cursor = (cursor + 1) % links.size();
    }

    public void remove(int index) {
        if (index >= 0 && index < links.size()) links.remove(index);
    }

    public List<BlockPos> list() {
        return links;
    }

    public boolean isEmpty() {
        return links.isEmpty();
    }

    public int size() {
        return links.size();
    }

    public int toggle(BlockPos pos) {
        if (links.remove(pos)) return 0;
        if (links.size() >= MAX) return -1;
        links.add(pos.immutable());
        return 1;
    }

    public void clear() {
        links.clear();
        cursor = 0;
    }

    public void save(CompoundTag tag, String key) {
        long[] values = new long[links.size()];
        for (int i = 0; i < values.length; i++) values[i] = links.get(i).asLong();
        if (values.length > 0) tag.putLongArray(key, values);
        if (roundRobin) tag.putBoolean(key + "_rr", true);
    }

    public void load(CompoundTag tag, String key) {
        links.clear();
        roundRobin = tag.getBoolean(key + "_rr");
        cursor = 0;
        if (!tag.contains(key)) return;
        for (long value : tag.getLongArray(key)) {
            if (links.size() < MAX) links.add(BlockPos.of(value));
        }
    }
}
