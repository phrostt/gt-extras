package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import java.util.HashMap;
import java.util.Map;

public final class BridgeAbilities {

    private static final Map<String, PartAbility> INPUT = new HashMap<>();
    private static final Map<String, PartAbility> OUTPUT = new HashMap<>();

    private BridgeAbilities() {}

    public static synchronized PartAbility input(String bridge) {
        return INPUT.computeIfAbsent(bridge, id -> new PartAbility("gtextras_bridge_" + id + "_input"));
    }

    public static synchronized PartAbility output(String bridge) {
        return OUTPUT.computeIfAbsent(bridge, id -> new PartAbility("gtextras_bridge_" + id + "_output"));
    }
}
