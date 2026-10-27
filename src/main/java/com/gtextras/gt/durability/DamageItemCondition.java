package com.gtextras.gt.durability;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import com.gtextras.gt.ExtrasConditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class DamageItemCondition extends RecipeCondition<DamageItemCondition> {

    public static final Codec<DamageItemCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition
            .isReverse(instance)
            .and(Codec.STRING.fieldOf("item").forGetter(DamageItemCondition::getItemId))
            .and(Codec.INT.fieldOf("amount").forGetter(DamageItemCondition::getAmount))
            .and(Codec.BOOL.optionalFieldOf("label", false).forGetter(DamageItemCondition::isLabel))
            .apply(instance, DamageItemCondition::new));

    private static final Map<RecipeLogic, GTRecipe> PAID = Collections.synchronizedMap(new WeakHashMap<>());

    private String itemId;
    private int amount;
    private boolean label;

    public DamageItemCondition() {
        this(false, "minecraft:air", 1, false);
    }

    public DamageItemCondition(String itemId, int amount) {
        this(false, itemId, amount, false);
    }

    public DamageItemCondition(String itemId, int amount, boolean label) {
        this(false, itemId, amount, label);
    }

    public DamageItemCondition(boolean isReverse, String itemId, int amount, boolean label) {
        super(isReverse);
        this.itemId = itemId;
        this.amount = Math.max(1, amount);
        this.label = label;
    }

    public boolean isLabel() {
        return label;
    }

    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) return false;
        if (itemId.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(itemId.substring(1)));
            return stack.is(tag);
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        return item != null && stack.getItem() == item;
    }

    public Component displayName() {
        if (itemId.startsWith("#")) return Component.literal(itemId);
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
        return item == null ? Component.literal(itemId) : item.getDescription();
    }

    @Override
    public RecipeConditionType<DamageItemCondition> getType() {
        return ExtrasConditions.DAMAGE_ITEM;
    }

    @Override
    public String getTranslationKey() {
        return "gtextras.recipe.condition.damage_item";
    }

    @Override
    public Component getTooltips() {
        if (label) return displayName();
        return Component.translatable(getTranslationKey(), amount);
    }

    public boolean hasUsable(RecipeLogic logic) {
        if (label) return true;
        for (IRecipeHandler<?> handler : logic.machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (handler instanceof IItemHandlerModifiable items) {
                for (int i = 0; i < items.getSlots(); i++) {
                    ItemStack stack = items.getStackInSlot(i);
                    if (matches(stack) && stack.getMaxDamage() - stack.getDamageValue() >= amount) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void clearPaid(RecipeLogic logic) {
        PAID.remove(logic);
    }

    public void consume(RecipeLogic logic, GTRecipe recipe) {
        if (label) return;
        PAID.put(logic, recipe);
        takeDurability(logic);
    }

    private void takeDurability(RecipeLogic logic) {
        for (IRecipeHandler<?> handler : logic.machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)) {
            if (handler instanceof IItemHandlerModifiable items) {
                for (int i = 0; i < items.getSlots(); i++) {
                    ItemStack stack = items.getStackInSlot(i);
                    if (matches(stack)) {
                        ItemStack copy = stack.copy();
                        RandomSource random = RandomSource.create();
                        boolean broke = false;
                        for (int n = 0; n < amount && !broke; n++) {
                            broke = copy.hurt(1, random, null);
                        }
                        items.setStackInSlot(i, broke ? ItemStack.EMPTY : copy);
                        return;
                    }
                }
            }
        }
    }

    @Override
    protected boolean testCondition(GTRecipe recipe, RecipeLogic recipeLogic) {
        if (!label && PAID.get(recipeLogic) == recipe && recipeLogic.getStatus() == RecipeLogic.Status.WORKING) {
            return true;
        }
        if (hasUsable(recipeLogic)) {
            return true;
        }
        RecipeLogic.putFailureReason(recipeLogic, recipe,
                Component.translatable("gtextras.recipe.damage_item_missing", amount, displayName()));
        return false;
    }

    @Override
    public DamageItemCondition createTemplate() {
        return new DamageItemCondition();
    }
}
