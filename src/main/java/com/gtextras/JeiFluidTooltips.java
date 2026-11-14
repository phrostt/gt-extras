package com.gtextras;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * GregTech's fluid tooltip (formula, state, temperature, ...) on fluids in JEI's
 * ingredient list, bookmarks and recipe screens. JEI builds those tooltips itself and
 * never asks GregTech, so this hooks Forge's tooltip gather event instead of JEI
 * internals: it asks the JEI runtime which fluid is under the mouse and adds GT's
 * lines right after the name. No mixins, so it does not depend on JEI's class layout.
 */
public final class JeiFluidTooltips {

    /** The IJeiRuntime, held as Object so this class loads without JEI present. */
    private static volatile Object runtime;

    private JeiFluidTooltips() {}

    public static void register() {
        if (!ModList.get().isLoaded("jei") || !ModList.get().isLoaded("gtceu")) {
            return;
        }
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, false, RenderTooltipEvent.GatherComponents.class,
                JeiFluidTooltips::onGather);
    }

    static void setRuntime(Object jeiRuntime) {
        runtime = jeiRuntime;
    }

    static void clearRuntime() {
        runtime = null;
    }

    private static void onGather(RenderTooltipEvent.GatherComponents event) {
        Object rt = runtime;
        if (rt == null) {
            return;
        }
        try {
            FluidStack stack = Hovered.find(rt);
            if (stack == null || stack.isEmpty()) {
                return;
            }
            TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips
                    ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
            List<Component> lines = new ArrayList<>();
            com.gregtechceu.gtceu.client.TooltipsHandler.appendFluidTooltips(stack, lines::add, flag);
            if (lines.isEmpty()) {
                return;
            }
            List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
            String first = lines.get(0).getString();
            for (Either<FormattedText, TooltipComponent> e : elements) {
                if (e.left().isPresent() && e.left().get().getString().equals(first)) {
                    return; // GT (or JEI) already added them
                }
            }
            int at = Math.min(1, elements.size());
            for (Component line : lines) {
                elements.add(at++, Either.left(line));
            }
        } catch (Throwable t) {
            GTExtras.LOGGER.debug("[{}] JEI fluid tooltip failed", GTExtras.MOD_ID, t);
        }
    }

    /** Isolated so JEI classes are only touched once a runtime exists. */
    private static final class Hovered {
        static FluidStack find(Object rt) {
            mezz.jei.api.runtime.IJeiRuntime jei = (mezz.jei.api.runtime.IJeiRuntime) rt;
            var type = mezz.jei.api.forge.ForgeTypes.FLUID_STACK;
            FluidStack s = jei.getIngredientListOverlay().getIngredientUnderMouse(type);
            if (s != null) {
                return s;
            }
            s = jei.getBookmarkOverlay().getIngredientUnderMouse(type);
            if (s != null) {
                return s;
            }
            return jei.getRecipesGui().getIngredientUnderMouse(type).orElse(null);
        }
    }
}
