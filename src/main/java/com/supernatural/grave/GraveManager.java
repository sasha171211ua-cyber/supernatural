package com.supernatural.grave;

import com.supernatural.block.ModBlocks;
import com.supernatural.block.RemainsBlock;
import com.supernatural.entity.GhostEntity;
import com.supernatural.entity.ModEntities;
import com.supernatural.ghost.GhostType;
import com.supernatural.item.ModItems;
import com.supernatural.util.LightEffects;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ItemScatterer;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Керує захороненнями: індекс останків у завантажених чанках, поява привидів, спалювання й нагороди.
 * Зберігання: GraveData (PersistentState). Нічого не скануємо щотіку: індекс будується один раз при завантаженні чанка.
 */
public final class GraveManager {
    private static final PersistentState.Type<GraveData> TYPE =
            new PersistentState.Type<>(GraveData::new, GraveData::fromNbt, null);
    private static final Map<RegistryKey<World>, Set<Long>> LOADED = new HashMap<>();

    private GraveManager() {
    }

    public static GraveData data(ServerWorld w) {
        return w.getPersistentStateManager().getOrCreate(TYPE, "supernatural_graves");
    }

    // ---------- індекс ----------
    public static void onChunkLoad(ServerWorld world, WorldChunk chunk) {
        ChunkSection[] sections = chunk.getSectionArray();
        int minSection = chunk.getBottomSectionCoord();
        ChunkPos cp = chunk.getPos();
        for (int i = 0; i < sections.length; i++) {
            ChunkSection s = sections[i];
            if (s == null || s.isEmpty()) continue;
            if (!s.hasAny(st -> st.getBlock() instanceof RemainsBlock)) continue;
            int baseY = (minSection + i) << 4;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        if (s.getBlockState(x, y, z).getBlock() instanceof RemainsBlock) {
                            BlockPos p = new BlockPos(cp.getStartX() + x, baseY + y, cp.getStartZ() + z);
                            LOADED.computeIfAbsent(world.getRegistryKey(), k -> new HashSet<>()).add(p.asLong());
                        }
                    }
                }
            }
        }
    }

    public static void onChunkUnload(ServerWorld world, WorldChunk chunk) {
        Set<Long> set = LOADED.get(world.getRegistryKey());
        if (set == null) return;
        ChunkPos cp = chunk.getPos();
        set.removeIf(l -> new ChunkPos(BlockPos.fromLong(l)).equals(cp));
    }

    // ---------- щотіку (раз на 10/20 тиків) ----------
    public static void onWorldTick(ServerWorld world) {
        long t = world.getTime();
        if (t % 10 == 0) LightEffects.process(world);
        if (t % 20 != 0) return;
        Set<Long> set = LOADED.get(world.getRegistryKey());
        if (set == null || set.isEmpty()) return;
        List<ServerPlayerEntity> players = world.getPlayers();
        if (players.isEmpty()) return;
        GraveData data = data(world);
        Iterator<Long> it = set.iterator();
        while (it.hasNext()) {
            long key = it.next();
            BlockPos pos = BlockPos.fromLong(key);
            if (!world.isChunkLoaded(pos)) continue;
            BlockState st = world.getBlockState(pos);
            if (!(st.getBlock() instanceof RemainsBlock rb)) {
                it.remove();
                continue;
            }
            GraveData.Grave g = data.get(key);
            if (g != null && g.status == GraveData.DESTROYED) continue;
            boolean near = false;
            for (ServerPlayerEntity p : players) {
                double dx = p.getX() - pos.getX();
                double dy = p.getY() - pos.getY();
                double dz = p.getZ() - pos.getZ();
                if (dx * dx + dy * dy + dz * dz < 40.0 * 40.0) { near = true; break; }
            }
            if (!near) continue;
            if (g == null) g = data.getOrCreate(key);
            if (g.status == GraveData.SPAWNED && g.ghost != null && world.getEntity(g.ghost) == null
                    && t - g.lastSeen > 6000) {
                g.status = GraveData.DORMANT; // привид зник (напр. /kill): дозволяємо одну нову появу
                data.markDirty();
            }
            if (g.status == GraveData.DORMANT) spawnGhost(world, pos, rb, g, key, data);
        }
    }

    private static void spawnGhost(ServerWorld sw, BlockPos remains, RemainsBlock rb, GraveData.Grave g,
                                   long key, GraveData data) {
        GhostEntity e = ModEntities.GHOST.create(sw);
        if (e == null) return;
        e.setGhostType(rb.type);
        double x = remains.getX() + 0.5, y = remains.getY() + 4.1, z = remains.getZ() + 0.5;
        e.refreshPositionAndAngles(x, y, z, sw.random.nextFloat() * 360f, 0f);
        e.bindGrave(key, remains, BlockPos.ofFloored(x, y, z));
        sw.spawnEntity(e);
        g.status = GraveData.SPAWNED;
        g.ghost = e.getUuid();
        g.lastSeen = sw.getTime();
        data.markDirty();
        sw.spawnParticles(ParticleTypes.SOUL, x, y + 0.5, z, 25, 0.4, 0.6, 0.4, 0.03);
        sw.playSound(null, BlockPos.ofFloored(x, y, z), SoundEvents.ENTITY_VEX_AMBIENT,
                SoundCategory.HOSTILE, 1.0f, 0.8f);
    }

    // ---------- виклики від привида ----------
    /** true, якщо цей привид має право представляти захоронення (захист від дублікатів). */
    public static boolean claim(ServerWorld sw, long key, UUID ghost) {
        GraveData data = data(sw);
        GraveData.Grave g = data.get(key);
        if (g == null) return true;
        if (g.status == GraveData.DESTROYED) return false;
        if (g.ghost == null || g.ghost.equals(ghost)) {
            g.ghost = ghost;
            g.status = GraveData.SPAWNED;
            data.markDirty();
            return true;
        }
        if (sw.getEntity(g.ghost) != null) return false;
        g.ghost = ghost;
        g.status = GraveData.SPAWNED;
        data.markDirty();
        return true;
    }

    public static void touch(ServerWorld sw, long key, UUID ghost) {
        GraveData data = data(sw);
        GraveData.Grave g = data.get(key);
        if (g == null || !ghost.equals(g.ghost)) return;
        g.lastSeen = sw.getTime();
        data.markDirty();
    }

    // ---------- спалювання останків ----------
    public static void burn(ServerWorld sw, BlockPos pos, RemainsBlock rb, ServerPlayerEntity player) {
        GraveData data = data(sw);
        long key = pos.asLong();
        GraveData.Grave g = data.getOrCreate(key);
        if (g.status == GraveData.DESTROYED || g.reward) return; // нагорода лише одна, захоронення завершується один раз
        g.status = GraveData.DESTROYED;
        g.reward = true;
        data.markDirty();

        sw.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
        sw.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 30, 0.3, 0.2, 0.3, 0.03);
        sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);

        GhostEntity ghost = null;
        if (g.ghost != null && sw.getEntity(g.ghost) instanceof GhostEntity ge) ghost = ge;
        if (ghost == null) { // привида ще не було поруч: створюємо, щоб програти розчинення й видати нагороду
            ghost = ModEntities.GHOST.create(sw);
            if (ghost == null) return;
            ghost.setGhostType(rb.type);
            ghost.refreshPositionAndAngles(player.getX(), player.getY(), player.getZ(), 0f, 0f);
            sw.spawnEntity(ghost);
        }
        ghost.beginDestruction(player);
    }

    // ---------- нагороди ----------
    public static void grantRewards(ServerWorld sw, GhostType t, Vec3d at) {
        var r = sw.random;
        if (t == GhostType.PEACEFUL) {
            int roll = r.nextInt(100);
            Item artifact;
            if (roll < 60) artifact = ModItems.WARNING_AMULET;            // звичайний
            else if (roll < 85) artifact = ModItems.PROTECTIVE_TALISMAN;  // незвичайний
            else if (roll < 97) artifact = ModItems.GRAVE_DETECTOR;       // рідкісний
            else artifact = ModItems.HUNTER_RELIC;                        // легендарний
            ItemScatterer.spawn(sw, at.x, at.y + 0.5, at.z, new ItemStack(artifact));
            return;
        }
        int ecto = r.nextBetween(t.ectoMin, t.ectoMax); // обидві межі включно
        if (ecto > 0) ItemScatterer.spawn(sw, at.x, at.y + 0.5, at.z, new ItemStack(ModItems.ECTOPLASM, ecto));
        if (r.nextInt(100) < t.trophyChance) {
            Item[] trophies = {ModItems.GHOST_SHARD, ModItems.OLD_AMULET, ModItems.RELIC_COMPONENT, ModItems.CURSED_FRAGMENT};
            ItemScatterer.spawn(sw, at.x, at.y + 0.5, at.z, new ItemStack(trophies[r.nextInt(trophies.length)]));
        }
    }
}
