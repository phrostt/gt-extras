package com.gtextras.gt.rotation.coupler;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

<<<<<<< HEAD
public class RotationMotorBlock extends RotatedPillarKineticBlock implements IBE<RotationMotorBlockEntity>,
        com.lowdragmc.lowdraglib.client.model.custommodel.ICTMPredicate {
=======
public class RotationMotorBlock extends RotatedPillarKineticBlock implements IBE<RotationMotorBlockEntity> {
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4

    public RotationMotorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public Class<RotationMotorBlockEntity> getBlockEntityClass() {
        return RotationMotorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends RotationMotorBlockEntity> getBlockEntityType() {
        return CouplerRegistry.MOTOR_TYPE.get();
    }
<<<<<<< HEAD

    @Override
    public boolean isConnected(net.minecraft.world.level.BlockAndTintGetter level, BlockState state, BlockPos pos,
                               BlockState other, BlockPos otherPos, Direction side) {
        if (other.getBlock() == this) return true;
        net.minecraft.resources.ResourceLocation key =
                net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(other.getBlock());
        if (key == null) return false;
        String ns = key.getNamespace();
        if (!ns.equals("gtceu") && !ns.equals(com.gtextras.GTExtras.MOD_ID)) return false;
        com.gregtechceu.gtceu.api.machine.MetaMachine machine =
                com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(level, otherPos);
        if (machine instanceof com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController
                || machine instanceof com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart) {
            return true;
        }
        String path = key.getPath();
        return path.endsWith("_casing") || path.contains("casing");
    }
=======
>>>>>>> a4459919e05790fa03cf1c067bf6f2c7c0aa0ca4
}
