package com.gtextras.gt.rotation.coupler.client;

import com.gtextras.gt.rotation.coupler.RotationMotorBlockEntity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
<<<<<<< HEAD
=======
import com.simibubi.create.AllBlocks;
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
<<<<<<< HEAD
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.data.ModelData;
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
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
<<<<<<< HEAD

        drawCasing(be, own, axis, poseStack, buffer, packedLight);

        float time = be.getLevel().getGameTime() + partialTick;
        net.minecraft.core.BlockPos at = be.getBlockPos();
        int sum = (axis == Direction.Axis.X ? 0 : at.getX()) + (axis == Direction.Axis.Y ? 0 : at.getY())
                + (axis == Direction.Axis.Z ? 0 : at.getZ());
        float offset = sum % 2 == 0 ? 22.5f : 0.0f;
        float degrees = (time * be.getSpeed() * 3.0f / 10.0f + offset) % 360.0f;
        Direction positive = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        Vector3f direction = new Vector3f(positive.getStepX(), positive.getStepY(), positive.getStepZ());

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(MotorClient.SHAFT_MODEL);
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.of(direction).rotationDegrees(degrees));
        if (axis == Direction.Axis.X) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0f));
        } else if (axis == Direction.Axis.Z) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), own, model,
                1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        BakedModel tip = Minecraft.getInstance().getModelManager().getModel(MotorClient.TIP_MODEL);
        Direction negative = positive.getOpposite();
        if (!blocksEnd(be, positive)) {
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), own,
                    tip, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
                    RenderType.cutout());
        }
        if (!blocksEnd(be, negative)) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.translate(-0.5, -0.5, -0.5);
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffer.getBuffer(RenderType.cutout()), own,
                    tip, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY,
                    RenderType.cutout());
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private static boolean blocksEnd(RotationMotorBlockEntity be, Direction dir) {
        net.minecraft.core.BlockPos other = be.getBlockPos().relative(dir);
        return be.getLevel().getBlockState(other).isSolidRender(be.getLevel(), other);
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
        if (counts.isEmpty()) {
            net.minecraft.world.level.block.Block steel = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(
                    new net.minecraft.resources.ResourceLocation("gtceu", "steel_machine_casing"));
            return steel == null ? null : steel.defaultBlockState();
        }
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
=======
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
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
        poseStack.popPose();
    }
}
