package com.gtextras.gt.rotation.coupler.client;

<<<<<<< HEAD
import com.gtextras.GTExtras;
import com.gtextras.gt.rotation.coupler.CouplerRegistry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;

public final class MotorClient {

    public static final ResourceLocation SHAFT_MODEL = new ResourceLocation(GTExtras.MOD_ID, "block/rotation_motor_shaft");

    public static final ResourceLocation TIP_MODEL = new ResourceLocation(GTExtras.MOD_ID, "block/rotation_motor_shaft_tip");

=======
import com.gtextras.gt.rotation.coupler.CouplerRegistry;

import net.minecraftforge.client.event.EntityRenderersEvent;

public final class MotorClient {

>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
    private MotorClient() {}

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CouplerRegistry.MOTOR_TYPE.get(), context -> new RotationMotorRenderer());
    }
<<<<<<< HEAD

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(SHAFT_MODEL);
        event.register(TIP_MODEL);
    }
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
}
