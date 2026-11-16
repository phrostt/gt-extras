package com.gtextras.mixin;

import de.maxhenkel.pipez.blocks.tileentity.PipeLogicTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.PipeTileEntity;
import de.maxhenkel.pipez.blocks.tileentity.types.EnergyPipeType;
import de.maxhenkel.pipez.blocks.tileentity.types.PipeType;
import com.gtextras.GTExtras;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipez only accepts energy pushed into a face that is set to Extract. This lets a push
 * through on any face, so a GregTech energy converter can feed a pipe whose face is not
 * set to Extract, with no per-tick transfer cap on that face; such a face only has a
 * capability to push into when PipezEnergyCapabilityMixin hands one out.
 * The face being pushed into is left out of the destinations so energy never loops back.
 */
@Mixin(value = EnergyPipeType.class, remap = false)
public abstract class PipezEnergyPushMixin {

    private static long gtxPushes = 0;

    private static final String RECEIVE = "receive(Lde/maxhenkel/pipez/blocks/tileentity/PipeLogicTileEntity;Lnet/minecraft/core/Direction;IZ)I";

    @Redirect(
            method = RECEIVE,
            at = @At(value = "INVOKE",
                    target = "Lde/maxhenkel/pipez/blocks/tileentity/PipeLogicTileEntity;isExtracting(Lnet/minecraft/core/Direction;)Z"),
            remap = false
    )
    private boolean gtextras$acceptPushOnAnyFace(PipeLogicTileEntity pipe, Direction side) {
        return true;
    }

    @Redirect(
            method = RECEIVE,
            at = @At(value = "INVOKE",
                    target = "Lde/maxhenkel/pipez/blocks/tileentity/types/EnergyPipeType;getRate(Lde/maxhenkel/pipez/blocks/tileentity/PipeLogicTileEntity;Lnet/minecraft/core/Direction;)I"),
            remap = false
    )
    private int gtextras$uncappedPushRate(EnergyPipeType self, PipeLogicTileEntity pipe, Direction side) {
        if (pipe.isExtracting(side)) {
            return self.getRate(pipe, side);
        }
        if (gtxPushes++ % 400 == 0) {
            GTExtras.LOGGER.info("[gtextras] push into pipe at {} face {} (rate uncapped; face rate would be {})",
                    pipe.getBlockPos(), side, self.getRate(pipe, side));
        }
        return Integer.MAX_VALUE;
    }

    @Redirect(
            method = RECEIVE,
            at = @At(value = "INVOKE",
                    target = "Lde/maxhenkel/pipez/blocks/tileentity/PipeLogicTileEntity;getSortedConnections(Lnet/minecraft/core/Direction;Lde/maxhenkel/pipez/blocks/tileentity/types/PipeType;)Ljava/util/List;"),
            remap = false
    )
    private List<PipeTileEntity.Connection> gtextras$skipSourceFace(PipeLogicTileEntity pipe, Direction side, PipeType<?> type) {
        List<PipeTileEntity.Connection> all = pipe.getSortedConnections(side, type);
        if (all.isEmpty()) {
            return all;
        }
        BlockPos source = pipe.getBlockPos().relative(side);
        List<PipeTileEntity.Connection> out = new ArrayList<>(all.size());
        for (PipeTileEntity.Connection c : all) {
            if (!c.getPos().equals(source)) {
                out.add(c);
            }
        }
        return out;
    }
}
