package com.supernatural.util;

import com.supernatural.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Невидимий бар'єр від лінії солі: працює як вертикальна колонка навколо кожного блока солі. */
public final class SaltBarrier {
    private SaltBarrier() {
    }

    public static boolean columnHasSalt(World w, int x, int y, int z) {
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int dy = -2; dy <= 2; dy++) {
            m.set(x, y + dy, z);
            if (w.getBlockState(m).isOf(ModBlocks.SALT_LINE)) return true;
        }
        return false;
    }

    public static boolean columnHasSalt(World w, Vec3d p) {
        return columnHasSalt(w, MathHelper.floor(p.x), MathHelper.floor(p.y), MathHelper.floor(p.z));
    }

    /** true, якщо відрізок a→b входить у колонку солі (коли a сам не всередині колонки). */
    public static boolean crosses(World w, Vec3d a, Vec3d b) {
        if (columnHasSalt(w, a)) return false;
        double len = a.distanceTo(b);
        int steps = Math.max(1, (int) Math.ceil(len / 0.25));
        for (int i = 1; i <= steps; i++) {
            Vec3d p = a.lerp(b, i / (double) steps);
            if (!w.isChunkLoaded(BlockPos.ofFloored(p))) return true;
            if (columnHasSalt(w, p)) return true;
        }
        return false;
    }
}
