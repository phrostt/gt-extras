package com.gtextras.gt.rotation;

import com.gtextras.GTExtras;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class RotationRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GTExtras.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GTExtras.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GTExtras.MOD_ID);

    public static final RegistryObject<Block> INTERFACE = BLOCKS.register("rotation_interface",
            () -> new RotationInterfaceBlock(BlockBehaviour.Properties.of().strength(2.0f, 3.0f)
                    .sound(SoundType.METAL).requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> INTERFACE_ITEM = ITEMS.register("rotation_interface",
            () -> new BlockItem(INTERFACE.get(), new Item.Properties()) {
                @Override
                public void appendHoverText(net.minecraft.world.item.ItemStack stack,
                                            net.minecraft.world.level.Level level,
                                            java.util.List<net.minecraft.network.chat.Component> tooltip,
                                            net.minecraft.world.item.TooltipFlag flag) {
                    tooltip.add(net.minecraft.network.chat.Component.translatable("gtextras.tooltip.rotation_interface")
                            .withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            });

    public static final RegistryObject<BlockEntityType<RotationInterfaceBlockEntity>> INTERFACE_TYPE =
            TYPES.register("rotation_interface", () -> BlockEntityType.Builder
                    .of((pos, state) -> new RotationInterfaceBlockEntity(RotationRegistry.INTERFACE_TYPE.get(), pos,
                            state), INTERFACE.get())
                    .build(null));

    private RotationRegistry() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TYPES.register(bus);
    }
}
