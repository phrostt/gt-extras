package com.gtextras.gt.rotation.coupler.client;

import com.gtextras.gt.rotation.coupler.RotationMotorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class RotationMotorRenderer implements BlockEntityRenderer<RotationMotorBlockEntity> {

    @Override
    public void render(RotationMotorBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (be.getLevel() == null) return;
        BlockState own = be.getBlockState();
        if (!own.hasProperty(RotatedPillarKineticBlock.AXIS)) return;
        Direction.Axis axis = own.getValue(RotatedPillarKineticBlock.AXIS);
        BlockState shaft = AllBlocks.SHAFT.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, axis);

        float time = be.getLevel().getGameTime() + partialTick;
        float degrees = (time * be.getSpeed() * 3.0f / 10.0f) % 360.0f;
        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Vector3f direction = new Vector3f(positive.getStepX(), positive.getStepY(), positive.getStepZ());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.of(direction).rotationDegrees(degrees));
        poseStack.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(shaft, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
