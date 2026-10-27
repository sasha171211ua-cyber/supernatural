package com.supernatural.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;

/** Джерела світла, які привиди тимчасово «погасили». Зберігається, щоб світло повернулось навіть після перезапуску. */
public class LightRestoreData extends PersistentState {
    public record Entry(long pos, NbtCompound state, long time) {
    }

    public static final PersistentState.Type<LightRestoreData> TYPE =
            new PersistentState.Type<>(LightRestoreData::new, LightRestoreData::fromNbt, null);

    public final List<Entry> entries = new ArrayList<>();

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtList list = new NbtList();
        for (Entry e : entries) {
            NbtCompound c = new NbtCompound();
            c.putLong("pos", e.pos());
            c.put("state", e.state());
            c.putLong("time", e.time());
            list.add(c);
        }
        nbt.put("entries", list);
        return nbt;
    }

    public static LightRestoreData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        LightRestoreData d = new LightRestoreData();
        NbtList list = nbt.getList("entries", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound c = list.getCompound(i);
            d.entries.add(new Entry(c.getLong("pos"), c.getCompound("state"), c.getLong("time")));
        }
        return d;
    }
}
