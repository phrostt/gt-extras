package com.gtextras;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ExtrasTab {

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GTExtras.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.gtextras"))
            .icon(() -> {
                List<Item> all = items();
                return all.isEmpty() ? new ItemStack(Items.REDSTONE) : new ItemStack(all.get(0));
            })
            .displayItems((params, output) -> {
                for (Item item : items()) {
                    output.accept(item);
                }
            })
            .build());

    private ExtrasTab() {}

    private static List<Item> items() {
        List<Item> list = new ArrayList<>();
        for (java.util.Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry
                : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation id = entry.getKey().location();
            if (GTExtras.MOD_ID.equals(id.getNamespace())) {
                list.add(entry.getValue());
            }
        }
        list.sort(Comparator.comparing(item -> ForgeRegistries.ITEMS.getKey(item).getPath()));
        return list;
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
