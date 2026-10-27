package com.supernatural.grave;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Постійні дані захоронень (ключ = позиція блока останків). Зберігаються у світі. */
public class GraveData extends PersistentState {
    public static final int DORMANT = 0;
    public static final int SPAWNED = 1;
    public static final int DESTROYED = 2;

    public static class Grave {
        public UUID id = UUID.randomUUID();
        public int status = DORMANT;
        public UUID ghost;
        public long lastSeen;
        public boolean reward;
    }

    public final Map<Long, Grave> graves = new HashMap<>();

    public Grave get(long key) {
        return graves.get(key);
    }

    public Grave getOrCreate(long key) {
        Grave g = graves.get(key);
        if (g == null) {
            g = new Grave();
            graves.put(key, g);
            markDirty();
        }
        return g;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Map.Entry<Long, Grave> e : graves.entrySet()) {
            Grave g = e.getValue();
            NbtCompound c = new NbtCompound();
            c.putLong("pos", e.getKey());
            c.putUuid("id", g.id);
            c.putInt("status", g.status);
            if (g.ghost != null) c.putUuid("ghost", g.ghost);
            c.putLong("lastSeen", g.lastSeen);
            c.putBoolean("reward", g.reward);
            list.add(c);
        }
        nbt.put("graves", list);
        return nbt;
    }

    public static GraveData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        GraveData d = new GraveData();
        NbtList list = nbt.getList("graves", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            Grave g = new Grave();
            if (c.containsUuid("id")) g.id = c.getUuid("id");
            g.status = c.getInt("status");
            if (c.containsUuid("ghost")) g.ghost = c.getUuid("ghost");
            g.lastSeen = c.getLong("lastSeen");
            g.reward = c.getBoolean("reward");
            d.graves.put(c.getLong("pos"), g);
        }
        return d;
    }
}
