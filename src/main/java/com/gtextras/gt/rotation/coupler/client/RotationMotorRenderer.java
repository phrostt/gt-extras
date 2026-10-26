package com.gtextras.gt.rotation.coupler.client;

import com.gtextras.gt.rotation.coupler.RotationMotorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.data.ModelData;
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

        drawCasing(be, own, axis, poseStack, buffer, packedLight);

        float time = be.getLevel().getGameTime() + partialTick;
        float degrees = (time * be.getSpeed() * 3.0f / 10.0f) % 360.0f;
        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Vector3f direction = new Vector3f(positive.getStepX(), positive.getStepY(), positive.getStepZ());

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.of(direction).rotationDegrees(degrees));
        poseStack.translate(-0.5, -0.5, -0.5);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(shaft);
        dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), shaft, model,
                1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();
    }

    private static BlockState sampleCasing(RotationMotorBlockEntity be) {
        net.minecraft.world.level.Level level = be.getLevel();
        java.util.Map<BlockState, Integer> counts = new java.util.HashMap<>();
        for (Direction dir : Direction.values()) {
            net.minecraft.core.BlockPos other = be.getBlockPos().relative(dir);
            BlockState state = level.getBlockState(other);
            if (state.isAir() || state.getBlock() == be.getBlockState().getBlock()
                    || level.getBlockEntity(other) != null || !state.isSolidRender(level, other)) {
                continue;
            }
            counts.merge(state, 1, Integer::sum);
        }
        BlockState best = null;
        int bestCount = 0;
        for (java.util.Map.Entry<BlockState, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                best = entry.getKey();
                bestCount = entry.getValue();
            }
        }
        return best;
    }

    private static void drawCasing(RotationMotorBlockEntity be, BlockState own, Direction.Axis axis,
                                   PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState casing = sampleCasing(be);
        if (casing == null) return;
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(casing);
        ModelData data = model.getModelData(be.getLevel(), be.getBlockPos(), casing, ModelData.EMPTY);
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(42L);
        com.mojang.blaze3d.vertex.VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(1.002f, 1.002f, 1.002f);
        poseStack.translate(-0.5, -0.5, -0.5);
        for (Direction dir : Direction.values()) {
            if (dir.getAxis() == axis) continue;
            random.setSeed(42L);
            for (net.minecraft.client.renderer.block.model.BakedQuad quad : model.getQuads(casing, dir, random, data,
                    RenderType.cutout())) {
                consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
            }
        }
        poseStack.popPose();
    }
}
