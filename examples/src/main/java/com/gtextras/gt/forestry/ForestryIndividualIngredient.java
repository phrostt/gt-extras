package com.gtextras.gt.forestry;

import com.gtextras.GTExtras;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import forestry.api.IForestryApi;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.ILifeStage;
import forestry.api.genetics.IIndividual;
import forestry.api.genetics.ISpeciesType;
import forestry.api.genetics.capability.IIndividualHandlerItem;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.stream.Stream;

public final class ForestryIndividualIngredient extends AbstractIngredient {

    public static final ResourceLocation ID = new ResourceLocation(GTExtras.MOD_ID, "forestry_individual");
    public static final Serializer SERIALIZER = new Serializer();
    private static final String DEFAULT_STAGE = "drone";

    private final ResourceLocation speciesType;
    private final ResourceLocation species;
    private final String stage;
    private ItemStack[] cache;

    private ForestryIndividualIngredient(ResourceLocation speciesType, ResourceLocation species, String stage) {
        super(Stream.empty());
        this.speciesType = speciesType;
        this.species = species;
        this.stage = stage;
    }

    public static void register() {
        CraftingHelper.register(ID, SERIALIZER);
    }

    private ILifeStage findStage(ISpeciesType<?, ?> type) {
        for (ILifeStage lifeStage : type.getLifeStages()) {
            if (stage.equals(lifeStage.getSerializedName())) {
                return lifeStage;
            }
        }
        return type.getDefaultStage();
    }

    private ItemStack[] build() {
        try {
            ISpeciesType<?, ?> type = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(speciesType);
            ItemStack stack = type.createStack(species, findStage(type));
            if (stack == null || stack.isEmpty()) {
                return new ItemStack[0];
            }
            return new ItemStack[] { stack };
        } catch (RuntimeException e) {
            GTExtras.LOGGER.warn("[{}] could not build {} {} ({})", GTExtras.MOD_ID, speciesType, species,
                    e.toString());
            return new ItemStack[0];
        }
    }

    @Override
    public ItemStack[] getItems() {
        ItemStack[] result = cache;
        if (result == null) {
            result = build();
            if (result.length > 0) {
                cache = result;
            }
        }
        return result;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        IIndividualHandlerItem handler = IIndividualHandlerItem.get(stack);
        if (handler == null) {
            return false;
        }
        IIndividual individual = handler.getIndividual();
        if (individual == null || !species.equals(individual.getSpecies().id())) {
            return false;
        }
        ILifeStage lifeStage = handler.getStage();
        return lifeStage != null && stage.equals(lifeStage.getSerializedName());
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
        json.addProperty("species_type", speciesType.toString());
        json.addProperty("species", species.toString());
        json.addProperty("stage", stage);
        return json;
    }

    private static ResourceLocation readId(String value, String field) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new JsonSyntaxException("'" + field + "' is not a valid id: " + value);
        }
        return id;
    }

    public static final class Serializer implements IIngredientSerializer<ForestryIndividualIngredient> {

        @Override
        public ForestryIndividualIngredient parse(JsonObject json) {
            ResourceLocation type = readId(
                    GsonHelper.getAsString(json, "species_type", ForestrySpeciesTypes.BEE.toString()), "species_type");
            ResourceLocation species = readId(GsonHelper.getAsString(json, "species"), "species");
            String stage = GsonHelper.getAsString(json, "stage", DEFAULT_STAGE);
            return new ForestryIndividualIngredient(type, species, stage);
        }

        @Override
        public ForestryIndividualIngredient parse(FriendlyByteBuf buffer) {
            ResourceLocation type = buffer.readResourceLocation();
            ResourceLocation species = buffer.readResourceLocation();
            String stage = buffer.readUtf();
            return new ForestryIndividualIngredient(type, species, stage);
        }

        @Override
        public void write(FriendlyByteBuf buffer, ForestryIndividualIngredient ingredient) {
            buffer.writeResourceLocation(ingredient.speciesType);
            buffer.writeResourceLocation(ingredient.species);
            buffer.writeUtf(ingredient.stage);
        }
    }
}
