package com.gtextras.gt.bridge;

public final class BridgeDef {

    public final String id;
    public final String displayName;
    public final int color;
    public final int returnColor;
    public final int resonanceCap;
    public final int resonanceDecayPerSecond;
    public final int range;

    public BridgeDef(String id, String displayName, int color, int returnColor, int resonanceCap,
                     int resonanceDecayPerSecond, int range) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.returnColor = returnColor;
        this.resonanceCap = Math.max(1, resonanceCap);
        this.resonanceDecayPerSecond = Math.max(0, resonanceDecayPerSecond);
        this.range = Math.max(1, range);
    }

    public int[] rgb(boolean returnBeam) {
        int value = returnBeam ? returnColor : color;
        return new int[] { (value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF };
    }
}
