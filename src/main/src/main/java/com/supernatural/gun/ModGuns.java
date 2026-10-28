package com.supernatural.gun;

import com.supernatural.SupernaturalMod;
import com.supernatural.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.EnumMap;
import java.util.Map;

/** Реєстрація вогнепальної зброї, набоїв і звуків. */
public final class ModGuns {
    private static final String[] AMMO = {"pistol_ammo", "shotgun_shell", "handmade_shell", "rifle_ammo", "spectre_round"};
    private static final Map<GunType, SoundEvent> FIRE = new EnumMap<>(GunType.class);
    private static final Map<GunType, SoundEvent> RELOAD_START = new EnumMap<>(GunType.class);
    private static final Map<GunType, SoundEvent> RELOAD_END = new EnumMap<>(GunType.class);

    private ModGuns() {
    }

    public static SoundEvent fireSound(GunType t) {
        return FIRE.get(t);
    }

    public static SoundEvent reloadStartSound(GunType t) {
        return RELOAD_START.get(t);
    }

    public static SoundEvent reloadEndSound(GunType t) {
        return RELOAD_END.get(t);
    }

    public static void register() {
        for (String a : AMMO) {
            Item item = Registry.register(Registries.ITEM, SupernaturalMod.id(a), new Item(new Item.Settings()));
            ModItems.addToTab(item);
        }
        for (GunType t : GunType.values()) {
            Identifier fire = SupernaturalMod.id("item." + t.id + ".fire");
            Identifier rs = SupernaturalMod.id("item." + t.id + ".reload_start");
            Identifier re = SupernaturalMod.id("item." + t.id + ".reload_end");
            FIRE.put(t, Registry.register(Registries.SOUND_EVENT, fire, SoundEvent.of(fire)));
            RELOAD_START.put(t, Registry.register(Registries.SOUND_EVENT, rs, SoundEvent.of(rs)));
            RELOAD_END.put(t, Registry.register(Registries.SOUND_EVENT, re, SoundEvent.of(re)));
            Item gun = Registry.register(Registries.ITEM, SupernaturalMod.id(t.id), new GunItem(t));
            ModItems.addToTab(gun);
        }
    }
}
