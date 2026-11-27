package com.gtextras.gt.bridge;

import com.gregtechceu.gtceu.api.recipe.content.IContentSerializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;

public final class BridgeSerializer implements IContentSerializer<String> {

    public static final BridgeSerializer INSTANCE = new BridgeSerializer();

    private BridgeSerializer() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, String content) {
        buf.writeUtf(content);
    }

    @Override
    public String fromNetwork(FriendlyByteBuf buf) {
        return buf.readUtf();
    }

    @Override
    public String fromJson(JsonElement json) {
        return json.getAsString();
    }

    @Override
    public JsonElement toJson(String content) {
        return new JsonPrimitive(content);
    }

    @Override
    public String of(Object o) {
        return o instanceof String s ? s : String.valueOf(o);
    }

    @Override
    public String defaultValue() {
        return "";
    }

    @Override
    public Class<String> contentClass() {
        return String.class;
    }

    @Override
    public Codec<String> codec() {
        return Codec.STRING;
    }
}
