package com.gtextras.gt.bridge;

public record BridgePacket(String channel, String kind, String name, long amount) {

    public static final String ITEM = "item";
    public static final String FLUID = "fluid";
    public static final String TAG = "tag";
    public static final String DATA = "data";

    public static BridgePacket parse(String text) {
        if (text == null) throw new IllegalArgumentException("empty bridge packet");
        String[] parts = text.trim().split("\\s+");
        if (parts.length != 4) {
            throw new IllegalArgumentException("a bridge packet is '<bridge> <item|fluid|tag|data> <name> <amount>', got '"
                    + text + "'");
        }
        String kind = parts[1].toLowerCase(java.util.Locale.ROOT);
        if (!kind.equals(ITEM) && !kind.equals(FLUID) && !kind.equals(TAG) && !kind.equals(DATA)) {
            throw new IllegalArgumentException("unknown bridge packet kind '" + parts[1] + "' in '" + text + "'");
        }
        long amount;
        try {
            amount = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad amount '" + parts[3] + "' in bridge packet '" + text + "'");
        }
        String name = parts[2].startsWith("#") ? parts[2].substring(1) : parts[2];
        return new BridgePacket(parts[0], kind, name, Math.max(0L, amount));
    }

    public String key() {
        return kind + " " + name;
    }

    public BridgePacket withAmount(long value) {
        return new BridgePacket(channel, kind, name, Math.max(0L, value));
    }

    @Override
    public String toString() {
        return channel + " " + kind + " " + name + " " + amount;
    }
}
