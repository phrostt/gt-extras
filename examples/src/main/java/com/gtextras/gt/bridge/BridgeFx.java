package com.gtextras.gt.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import org.joml.Vector3f;

public final class BridgeFx {

    private BridgeFx() {}

    public static void line(Level level, BlockPos from, BlockPos to, int[] color, float size, double density) {
        if (!(level instanceof ServerLevel server) || from.equals(to)) return;
        double x1 = from.getX() + 0.5, y1 = from.getY() + 0.5, z1 = from.getZ() + 0.5;
        double dx = to.getX() + 0.5 - x1, dy = to.getY() + 0.5 - y1, dz = to.getZ() + 0.5 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(2, (int) (length * density));
        DustParticleOptions dust = new DustParticleOptions(
                new Vector3f(color[0] / 255f, color[1] / 255f, color[2] / 255f), size);
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            server.sendParticles(dust, x1 + dx * t, y1 + dy * t, z1 + dz * t, 1, 0, 0, 0, 0);
        }
    }
}
