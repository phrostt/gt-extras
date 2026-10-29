package com.gtextras.mixin;

import com.gtextras.CoilConfig;
import com.gtextras.CoilIndex;
import com.gtextras.CoilInfo;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "com.lowdragmc.lowdraglib.jei.ModularUIRecipeCategory", remap = false)
public abstract class CoilJeiSlotMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
            method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Ljava/lang/Object;Lmezz/jei/api/recipe/IFocusGroup;)V",
            at = @At("TAIL"),
            remap = false,
            require = 0
    )
    private void gtextras$addRequirementSlot(IRecipeLayoutBuilder builder, Object recipe, IFocusGroup focuses, CallbackInfo ci) {
        try {
            if (recipe == null || !recipe.getClass().getName().equals("com.gregtechceu.gtceu.api.recipe.GTRecipe")) {
                return;
            }
            if (!(this instanceof IRecipeCategory category)) {
                return;
            }
            CoilConfig config = CoilConfig.get();
            if (!config.enabled || !config.jeiEnabled
                    || net.minecraftforge.fml.ModList.get().isLoaded("emi")) {
                return;
            }
            CoilInfo.Result info = CoilInfo.resolve(config, category.getRecipeType().getUid(), recipe);
            List<com.gtextras.ExtraBadges.Badge> badges = com.gtextras.ExtraBadges.of(config, recipe);
            if (info == null && badges.isEmpty()) {
                return;
            }
            CoilInfo.Layout layout = CoilInfo.layout(config, category.getRecipeType().getUid(), recipe, category.getWidth(), category.getHeight());

            for (int i = 0; i < badges.size(); i++) {
                com.gtextras.ExtraBadges.Badge badge = badges.get(i);
                List<Component> lines = badge.tooltip();
                net.minecraft.world.level.material.Fluid fluid =
                        badge.icon().getItem() instanceof net.minecraft.world.item.BucketItem bucket
                                ? bucket.getFluid() : net.minecraft.world.level.material.Fluids.EMPTY;
                if (fluid != net.minecraft.world.level.material.Fluids.EMPTY
                       ) {
                    RecipeIngredientRole role = com.gtextras.ExtraBadges.isInput(badge) ? RecipeIngredientRole.INPUT
                            : RecipeIngredientRole.OUTPUT;
                    builder.addSlot(role, com.gtextras.ExtraBadges.cellX(layout, i) + 1,
                                    com.gtextras.ExtraBadges.cellY(layout, i) + 1)
                            .addFluidStack(fluid, 1L)
                            .addRichTooltipCallback((view, tooltip) -> lines.forEach(tooltip::add));
                    continue;
                }
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, com.gtextras.ExtraBadges.cellX(layout, i) + 1,
                                com.gtextras.ExtraBadges.cellY(layout, i) + 1)
                        .addItemStack(badge.icon())
                        .addRichTooltipCallback((view, tooltip) -> lines.forEach(tooltip::add));
            }
            if (info == null) {
                CoilInfo.markSlotted(recipe);
                return;
            }

            if (info.hasCoil()) {
                List<ItemStack> stacks = new ArrayList<>();
                for (CoilIndex.Coil coil : CoilIndex.allMatching(info.mode(), info.required())) {
                    stacks.add(coil.stack());
                }
                if (stacks.isEmpty()) {
                    stacks.add(info.coil().stack());
                }
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, layout.iconX() + 1, layout.iconY() + 1)
                        .addItemStacks(stacks)
                        .addRichTooltipCallback((view, tooltip) -> {
                            tooltip.add(Component.translatable("gtextras.tooltip.requires", info.coil().name())
                                    .withStyle(ChatFormatting.GOLD));
                            tooltip.add(Component.translatable("gtextras.tooltip." + info.mode(), info.required())
                                    .withStyle(ChatFormatting.GRAY));
                        });
            } else if (info.hasTarget()) {
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, layout.dataX() + 1, layout.iconY() + 1)
                        .addItemStack(info.research())
                        .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                                Component.translatable("gtextras.tooltip.research_data").withStyle(ChatFormatting.GOLD)));
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, layout.iconX() + 1, layout.iconY() + 1)
                        .addItemStack(info.target())
                        .addRichTooltipCallback((view, tooltip) -> tooltip.add(
                                Component.translatable("gtextras.tooltip.research_target").withStyle(ChatFormatting.GOLD)));
            } else {
                builder.addSlot(RecipeIngredientRole.RENDER_ONLY, layout.iconX() + 1, layout.iconY() + 1)
                        .addItemStack(info.research())
                        .addRichTooltipCallback((view, tooltip) -> {
                            tooltip.add(Component.translatable("gtextras.tooltip.research").withStyle(ChatFormatting.GOLD));
                            tooltip.add(info.research().getHoverName().copy().withStyle(ChatFormatting.GRAY));
                        });
            }
            CoilInfo.markSlotted(recipe);
        } catch (Throwable t) {
            // leave the decorator fallback in charge
        }
    }
}
