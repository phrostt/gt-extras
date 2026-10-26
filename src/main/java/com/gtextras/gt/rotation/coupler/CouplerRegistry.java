package com.gtextras.gt.rotation.coupler;

import com.gtextras.GTExtras;

import net.minecraft.core.registries.Registries;
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

public final class CouplerRegistry {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GTExtras.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GTExtras.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GTExtras.MOD_ID);

    public static final RegistryObject<Block> COUPLER = BLOCKS.register("rotation_coupler",
            () -> new RotationCouplerBlock(BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
                    .noOcclusion().requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> COUPLER_ITEM = ITEMS.register("rotation_coupler",
            () -> new BlockItem(COUPLER.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<RotationCouplerBlockEntity>> COUPLER_TYPE =
            TYPES.register("rotation_coupler", () -> BlockEntityType.Builder
                    .of((pos, state) -> new RotationCouplerBlockEntity(CouplerRegistry.COUPLER_TYPE.get(), pos, state), COUPLER.get()).build(null));

    public static final RegistryObject<Block> MOTOR = BLOCKS.register("rotation_motor",
            () -> new RotationMotorBlock(BlockBehaviour.Properties.of().strength(2.0f, 3.0f).sound(SoundType.WOOD)
                    .noOcclusion().requiresCorrectToolForDrops()));

    public static final RegistryObject<Item> MOTOR_ITEM = ITEMS.register("rotation_motor",
            () -> new BlockItem(MOTOR.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<RotationMotorBlockEntity>> MOTOR_TYPE =
            TYPES.register("rotation_motor", () -> BlockEntityType.Builder
                    .of((pos, state) -> new RotationMotorBlockEntity(CouplerRegistry.MOTOR_TYPE.get(), pos, state), MOTOR.get()).build(null));

    private CouplerRegistry() {}

    public static void init(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        TYPES.register(bus);
    }
}
