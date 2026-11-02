package com.gtextras.gt.bridge;

public final class BridgeDef {

    public final String id;
    public final String displayName;
    public final int color;
    public final int returnColor;
    public final int resonanceCap;
    public final int resonanceDecayPerSecond;
    public final int range;
    public final boolean energyEnabled;
    public final int energyRange;
    public final int energyLossPercent;
    public final long energyMaxAmperage;
    public final int energyBufferTicks;
    public final int[] tiers;
    public final String hatchItem;
    public final long energyBufferSize;
    public final int[] energyAmperages;
    public final boolean energyAllowTierMismatch;

    public BridgeDef(String id, String displayName, int color, int returnColor, int resonanceCap,
                     int resonanceDecayPerSecond, int range, boolean energyEnabled, int energyRange,
                     int energyLossPercent, long energyMaxAmperage, int energyBufferTicks, int[] tiers, String hatchItem, long energyBufferSize,
                     int[] energyAmperages, boolean energyAllowTierMismatch) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.returnColor = returnColor;
        this.resonanceCap = Math.max(1, resonanceCap);
        this.resonanceDecayPerSecond = Math.max(0, resonanceDecayPerSecond);
        this.range = Math.max(1, range);
        this.energyEnabled = energyEnabled;
        this.energyRange = Math.max(1, energyRange);
        this.energyLossPercent = Math.max(0, Math.min(99, energyLossPercent));
        this.energyMaxAmperage = Math.max(0L, energyMaxAmperage);
        this.energyBufferTicks = Math.max(1, energyBufferTicks);
        this.tiers = tiers;
        this.energyBufferSize = Math.max(0L, energyBufferSize);
        this.energyAmperages = energyAmperages == null ? new int[0] : energyAmperages;
        this.energyAllowTierMismatch = energyAllowTierMismatch;
        this.hatchItem = hatchItem == null || hatchItem.isBlank() ? "minecraft:ender_eye" : hatchItem.trim();
    }

    public int maxLinkRange() {
        return energyEnabled ? Math.max(range, energyRange) : range;
    }

    public int[] rgb(boolean returnBeam) {
        int value = returnBeam ? returnColor : color;
        return new int[] { (value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF };
    }
}
