package com.gtextras.gt.bridge;

import com.gtextras.GTExtras;

import com.google.gson.JsonObject;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.stream.Stream;

public final class BridgeItemIngredient extends AbstractIngredient {

    public static final ResourceLocation ID = new ResourceLocation(GTExtras.MOD_ID, "bridge_item");
    public static final Serializer SERIALIZER = new Serializer();
    private static final String FALLBACK = "minecraft:ender_eye";

    private final String bridge;
    private final String spec;
    private final TagKey<Item> tag;
    private final Item item;

    private BridgeItemIngredient(String bridge, String spec) {
        super(Stream.empty());
        this.bridge = bridge;
        String use = spec;
        TagKey<Item> foundTag = null;
        Item foundItem = null;
        if (use.startsWith("#")) {
            ResourceLocation rl = ResourceLocation.tryParse(use.substring(1));
            if (rl != null) {
                foundTag = TagKey.create(Registries.ITEM, rl);
            }
        } else {
            ResourceLocation rl = ResourceLocation.tryParse(use);
            if (rl != null && ForgeRegistries.ITEMS.containsKey(rl)) {
                foundItem = ForgeRegistries.ITEMS.getValue(rl);
            }
        }
        if (foundTag == null && foundItem == null) {
            GTExtras.LOGGER.warn("[{}] bridge '{}' hatchItem '{}' is not an item or tag, using {}", GTExtras.MOD_ID,
                    bridge, spec, FALLBACK);
            use = FALLBACK;
            foundItem = Items.ENDER_EYE;
        }
        this.spec = use;
        this.tag = foundTag;
        this.item = foundItem;
    }

    public static void register() {
        CraftingHelper.register(ID, SERIALIZER);
    }

    private static String specFor(String bridge) {
        BridgeDef def = BridgeRegistry.get(bridge);
        return def == null ? FALLBACK : def.hatchItem;
    }

    @Override
    public ItemStack[] getItems() {
        if (tag != null) {
            return ForgeRegistries.ITEMS.tags().getTag(tag).stream().map(ItemStack::new).toArray(ItemStack[]::new);
        }
        return new ItemStack[] { new ItemStack(item) };
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null) return false;
        return tag != null ? stack.is(tag) : stack.is(item);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", ID.toString());
        json.addProperty("bridge", bridge);
        return json;
    }

    public static final class Serializer implements IIngredientSerializer<BridgeItemIngredient> {

        @Override
        public BridgeItemIngredient parse(JsonObject json) {
            String bridge = json.has("bridge") ? json.get("bridge").getAsString() : "";
            return new BridgeItemIngredient(bridge, specFor(bridge));
        }

        @Override
        public BridgeItemIngredient parse(FriendlyByteBuf buffer) {
            String bridge = buffer.readUtf();
            String spec = buffer.readUtf();
            return new BridgeItemIngredient(bridge, spec);
        }

        @Override
        public void write(FriendlyByteBuf buffer, BridgeItemIngredient ingredient) {
            buffer.writeUtf(ingredient.bridge);
            buffer.writeUtf(ingredient.spec);
        }
    }
}
