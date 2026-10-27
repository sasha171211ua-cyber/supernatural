package com.supernatural.util;

import com.supernatural.entity.GhostEntity;
import com.supernatural.ghost.GhostType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Мерехтіння й тимчасове «гасіння» факелів/ліхтарів біля привида. Блоки не знищуються: стан запам'ятовується й повертається. */
public final class LightEffects {
    private static final Set<Block> LIGHTS = Set.of(
            Blocks.TORCH, Blocks.WALL_TORCH, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH,
            Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.LANTERN, Blocks.SOUL_LANTERN);

    private LightEffects() {
    }

    public static LightRestoreData data(ServerWorld w) {
        return w.getPersistentStateManager().getOrCreate(LightRestoreData.TYPE, "supernatural_lights");
    }

    /** Один «імпульс» світлових ефектів навколо привида. strong = примусово довге гасіння. */
    public static void run(ServerWorld sw, GhostEntity g, GhostType t, boolean strong) {
        if (!t.hostile()) return;
        BlockPos c = g.getBlockPos();
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos p : BlockPos.iterate(c.add(-12, -6, -12), c.add(12, 6, 12))) {
            if (!sw.isChunkLoaded(p)) continue;
            if (LIGHTS.contains(sw.getBlockState(p).getBlock())) found.add(p.toImmutable());
        }
        if (found.isEmpty()) return;

        float r = sw.random.nextFloat();
        int count;
        int duration;
        switch (t) {
            case STRONG -> {
                if (r < 0.35f || strong) { count = 1; duration = 30; } else { count = 1 + sw.random.nextInt(2); duration = 6; }
            }
            case ANCIENT -> {
                if (r < 0.40f || strong) { count = 2 + sw.random.nextInt(2); duration = 50; } else { count = 1 + sw.random.nextInt(2); duration = 6; }
            }
            case CURSED -> {
                if (r < 0.50f || strong) { count = 2 + sw.random.nextInt(2); duration = 60; } else { count = 1 + sw.random.nextInt(2); duration = 6; }
            }
            default -> { count = 1 + sw.random.nextInt(2); duration = 6; }
        }

        Collections.shuffle(found, new java.util.Random());
        LightRestoreData d = data(sw);
        long now = sw.getTime();
        for (int i = 0; i < Math.min(count, found.size()); i++) {
            BlockPos p = found.get(i);
            boolean pending = false;
            for (LightRestoreData.Entry e : d.entries) {
                if (e.pos() == p.asLong()) { pending = true; break; }
            }
            if (pending) continue;
            BlockState s = sw.getBlockState(p);
            d.entries.add(new LightRestoreData.Entry(p.asLong(), NbtHelper.fromBlockState(s), now + duration));
            sw.setBlockState(p, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
            d.markDirty();
        }
    }

    /** Повертає погашені джерела світла, час яких вийшов. Викликається раз на 10 тиків. */
    public static void process(ServerWorld sw) {
        LightRestoreData d = data(sw);
        if (d.entries.isEmpty()) return;
        long now = sw.getTime();
        Iterator<LightRestoreData.Entry> it = d.entries.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            LightRestoreData.Entry e = it.next();
            if (now < e.time()) continue;
            BlockPos p = BlockPos.fromLong(e.pos());
            if (!sw.isChunkLoaded(p)) continue;
            BlockState saved = NbtHelper.toBlockState(Registries.BLOCK.getReadOnlyWrapper(), e.state());
            if (sw.getBlockState(p).isAir() && saved.canPlaceAt(sw, p)) {
                sw.setBlockState(p, saved, Block.NOTIFY_ALL);
            }
            it.remove();
            changed = true;
        }
        if (changed) d.markDirty();
    }
}
