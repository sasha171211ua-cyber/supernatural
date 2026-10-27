package com.supernatural.world;

import com.supernatural.block.ModBlocks;
import com.supernatural.ghost.GhostType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Підземна могила: надгробок на підлозі печери (навколо повітря) + камера 2×1 з останками під ним. */
public class GraveFeature extends Feature<DefaultFeatureConfig> {
    private static final GhostType[] ORDER = {
            GhostType.NORMAL, GhostType.EVIL, GhostType.STRONG,
            GhostType.CURSED, GhostType.ANCIENT, GhostType.PEACEFUL};
    // Ймовірності типів для підземель: 30 / 25 / 20 / 10 / 13 / 2
    private static final int[] WEIGHTS = {30, 25, 20, 10, 13, 2};

    public GraveFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    private static GhostType pick(Random r) {
        int roll = r.nextInt(100);
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += WEIGHTS[i];
            if (roll < sum) return ORDER[i];
        }
        return GhostType.NORMAL;
    }

    private static BlockState floorFor(GhostType t) {
        return switch (t) {
            case EVIL -> Blocks.BLACKSTONE.getDefaultState();
            case STRONG -> Blocks.STONE_BRICKS.getDefaultState();
            case CURSED -> Blocks.SOUL_SOIL.getDefaultState();
            case ANCIENT -> Blocks.CHISELED_STONE_BRICKS.getDefaultState();
            case PEACEFUL -> Blocks.MOSS_BLOCK.getDefaultState();
            default -> Blocks.COARSE_DIRT.getDefaultState();
        };
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> ctx) {
        StructureWorldAccess w = ctx.getWorld();
        BlockPos o = ctx.getOrigin(); // повітря над підлогою печери
        Random r = ctx.getRandom();

        // навколо надгробка має бути повітря: його клітинка, 4 сусіди й 2 блоки вгору
        if (!w.getBlockState(o).isAir()) return false;
        for (Direction d : Direction.Type.HORIZONTAL) {
            if (!w.getBlockState(o.offset(d)).isAir()) return false;
        }
        if (!w.getBlockState(o.up()).isAir() || !w.getBlockState(o.up(2)).isAir()) return false;

        // під ним суцільна порода без рідини (там камера)
        for (int i = 1; i <= 4; i++) {
            BlockPos p = o.down(i);
            if (w.getBlockState(p).isAir() || !w.getFluidState(p).isEmpty()) return false;
        }

        GhostType type = pick(r);
        BlockPos remains = o.down(3);
        BlockPos second = remains.offset(Direction.Type.HORIZONTAL.random(r));
        if (!w.getFluidState(second).isEmpty() || !w.getFluidState(second.down()).isEmpty()) return false;

        set(w, o, ModBlocks.tombstoneOf(type).getDefaultState());
        set(w, remains, ModBlocks.remainsOf(type).getDefaultState());
        set(w, second, ModBlocks.BURIAL_URN.getDefaultState());
        set(w, remains.down(), floorFor(type));
        set(w, second.down(), floorFor(type));
        return true;
    }

    private static void set(StructureWorldAccess w, BlockPos p, BlockState s) {
        w.setBlockState(p, s, Block.NOTIFY_LISTENERS);
    }
}
