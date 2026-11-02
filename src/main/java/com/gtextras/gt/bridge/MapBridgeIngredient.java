package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import java.util.Collections;
import java.util.List;

public final class MapBridgeIngredient extends AbstractMapIngredient {

    private final String channel;

    public MapBridgeIngredient(String channel) {
        this.channel = channel;
    }

    public static List<AbstractMapIngredient> from(String content) {
        if (content == null) return Collections.emptyList();
        String trimmed = content.trim();
        if (trimmed.isEmpty()) return Collections.emptyList();
        int space = trimmed.indexOf(' ');
        String channel = space < 0 ? trimmed : trimmed.substring(0, space);
        return Collections.singletonList(new MapBridgeIngredient(channel));
    }

    @Override
    protected int hash() {
        return 0x42524447 ^ channel.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (super.equals(o)) {
            return channel.equals(((MapBridgeIngredient) o).channel);
        }
        return false;
    }
}
