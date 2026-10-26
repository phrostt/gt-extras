package com.gtextras.gt.rotation.coupler.client;

import com.gtextras.gt.rotation.coupler.CouplerRegistry;

import net.minecraftforge.client.event.EntityRenderersEvent;

public final class MotorClient {

    private MotorClient() {}

    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CouplerRegistry.MOTOR_TYPE.get(), context -> new RotationMotorRenderer());
    }
}
