package com.gtextras.gt.tool;

import com.gtextras.GTExtras;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ExtrasItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GTExtras.MOD_ID);

    public static final RegistryObject<Item> LINK_TOOL = ITEMS.register("link_tool", LinkToolItem::new);

    private ExtrasItems() {}

    public static void init(IEventBus bus) {
        ITEMS.register(bus);
    }
}
