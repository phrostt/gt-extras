package com.gtextras.gt.rotation.coupler.client;

import com.gtextras.GTExtras;
import com.gtextras.gt.rotation.coupler.CouplerRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;

public final class MotorClient {

    public static final ResourceLocation SHAFT_MODEL = new ResourceLocation(GTExtras.MOD_ID, "block/rotation_motor_shaft");

    private MotorClient() {}

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CouplerRegistry.MOTOR_TYPE.get(), context -> new RotationMotorRenderer());
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(SHAFT_MODEL);
    }
}
