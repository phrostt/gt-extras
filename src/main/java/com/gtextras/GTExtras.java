package com.gtextras;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * GT Extras entry point: recipe-viewer additions on the client, GregTech hatches on both sides.
 */
@Mod(GTExtras.MOD_ID)
public class GTExtras {

    public static final String MOD_ID = "gtextras";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GTExtras() {
        if (net.minecraftforge.fml.ModList.get().isLoaded("gtceu")) {
            com.gtextras.gt.GTExtrasGT.init(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LOGGER.info("[{}] loaded, waiting for EMI", MOD_ID);
            RecipeIdCopy.register();
            net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus().addListener(RecipeIdCopy::registerKeys);
        }
    }
}
