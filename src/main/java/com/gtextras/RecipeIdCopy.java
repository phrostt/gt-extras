package com.gtextras;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;

/**
 * Pack-dev helper: shift + left click on a recipe's output item in the JEI recipe screen
 * copies the recipe id to the clipboard, for every recipe category. Holds no JEI types, so it is
 * safe to load without JEI; the JEI lookup lives in {@link JeiRecipeIdLookup}.
 */
public final class RecipeIdCopy {

    private static final String JEI_RECIPES_GUI = "mezz.jei.gui.recipes.RecipesGui";

    private RecipeIdCopy() {
    }

    static final KeyMapping COPY_KEY = new KeyMapping("key.gtextras.copy_recipe_id", net.minecraftforge.client.settings.KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM, org.lwjgl.glfw.GLFW.GLFW_KEY_I, "key.categories.gtextras");

    static void registerKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent event) {
        event.register(COPY_KEY);
    }

    static void onKey(ScreenEvent.KeyPressed.Pre event) {
        if (!CoilConfig.get().copyRecipeId) {
            return;
        }
        Screen screen = event.getScreen();
        if (!JEI_RECIPES_GUI.equals(screen.getClass().getName())) {
            return;
        }
        if (!COPY_KEY.isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        double x = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double y = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();
        String id;
        try {
            id = JeiRecipeIdLookup.findAny(screen, x, y);
        } catch (Throwable t) {
            GTExtras.LOGGER.warn("[{}] recipe id lookup failed", GTExtras.MOD_ID, t);
            return;
        }
        if (id == null || id.isEmpty()) {
            return;
        }
        mc.keyboardHandler.setClipboard(id);
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("gtextras.copied_recipe_id", id));
        }
        event.setCanceled(true);
    }

    static void register() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true,
                ScreenEvent.KeyPressed.Pre.class, RecipeIdCopy::onKey);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, true,
                ScreenEvent.MouseButtonPressed.Pre.class, RecipeIdCopy::onClick);
    }

    static void onClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getButton() != 0 || !Screen.hasShiftDown()) {
            return;
        }
        if (!CoilConfig.get().copyRecipeId) {
            return;
        }
        Screen screen = event.getScreen();
        if (!JEI_RECIPES_GUI.equals(screen.getClass().getName())) {
            return;
        }
        String id;
        try {
            id = JeiRecipeIdLookup.find(screen, event.getMouseX(), event.getMouseY());
        } catch (Throwable t) {
            GTExtras.LOGGER.warn("[{}] recipe id lookup failed", GTExtras.MOD_ID, t);
            return;
        }
        if (id == null || id.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        mc.keyboardHandler.setClipboard(id);
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("gtextras.copied_recipe_id", id));
        }
        event.setCanceled(true);
    }
}
