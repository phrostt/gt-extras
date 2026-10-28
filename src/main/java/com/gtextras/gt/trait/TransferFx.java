package com.gtextras.gt.trait;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import org.joml.Vector3f;

public final class TransferFx {

    public static final int[] MANA = { 70, 170, 255 };
    public static final int[] SOURCE = { 255, 40, 190 };
    public static final int[] BLOOD = { 190, 20, 30 };
    public static final int[] REMOVED = { 255, 60, 60 };

    private static Boolean ars;

    private TransferFx() {}

    private static boolean ars() {
        if (ars == null) ars = ModList.get().isLoaded("ars_nouveau");
        return ars;
    }

    public static void orb(Level level, BlockPos from, BlockPos to, int[] color) {
        if (level == null || level.isClientSide || !HatchConfig.particles()) return;
        if (from.equals(to)) return;
        if (ars()) {
            ArsOrb.spawn(level, from, to, color);
        } else {
            line(level, from, to, color, 0.6f);
        }
    }

    public static void line(Level level, BlockPos from, BlockPos to, int[] color, float size) {
        line(level, from, to, color, size, 2.5);
    }

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

    private static final class ArsOrb {

        static void spawn(Level level, BlockPos from, BlockPos to, int[] color) {
            level.addFreshEntity(new com.hollingsworth.arsnouveau.common.entity.EntityFollowProjectile(level, from,
                    to, color[0], color[1], color[2]));
        }
    }
}
