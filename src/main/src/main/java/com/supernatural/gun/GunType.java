package com.supernatural.gun;

/**
 * Характеристики зброї. Числа взяті з оригінального Just Enough Guns (data/jeg/guns/*.json):
 * damage — повна шкода пострілу (у дробовиків ділиться на кількість дробин), rate — тіки між пострілами,
 * spread — розкид, recoil — кут віддачі, maxAmmo, reloadTimer/extra — час перезарядки.
 */
public enum GunType {
    //                                 id                      ammo              cap  damage pellets rate spread recoil reload extra manual range
    WATERPIPE_SHOTGUN("waterpipe_shotgun",     "handmade_shell", 1,  20.0f, 18,  8, 15.0f, 10.0f, 30,  0, false,  60),
    CUSTOM_SMG("custom_smg",                   "pistol_ammo",   24,   2.8f,  1,  2,  4.0f,  1.0f, 35, 10, false, 160),
    REVOLVER("revolver",                       "pistol_ammo",    8,   5.0f,  1,  3,  3.0f,  4.0f, 20, 40, true,  160),
    SEMI_AUTO_PISTOL("semi_auto_pistol",       "pistol_ammo",   10,   5.5f,  1,  2,  2.5f,  2.0f, 37, 13, false, 160),
    COMBAT_PISTOL("combat_pistol",             "pistol_ammo",   15,   6.5f,  1,  2,  1.5f,  2.0f, 37, 13, false, 160),
    DOUBLE_BARREL_SHOTGUN("double_barrel_shotgun", "handmade_shell", 2, 24.0f, 22, 8, 25.0f, 10.0f, 37, 13, true,  36),
    PUMP_SHOTGUN("pump_shotgun",               "shotgun_shell",  6,  20.0f, 12, 22, 10.0f, 10.0f, 18,  6, true,   60),
    REPEATING_SHOTGUN("repeating_shotgun",     "shotgun_shell",  8,  25.0f, 12, 22,  8.0f, 10.0f, 16, 10, true,   60),
    HOLY_SHOTGUN("holy_shotgun",               "spectre_round",  8,  23.0f, 10, 14,  8.0f,  8.0f, 16, 10, true,   80),
    BOLT_ACTION_RIFLE("bolt_action_rifle",     "rifle_ammo",     4,  21.0f,  1, 28,  0.25f, 4.0f, 20, 10, true,  160),
    INFANTRY_RIFLE("infantry_rifle",           "rifle_ammo",     8,  10.5f,  1,  3,  3.0f,  3.0f, 30, 10, false, 160);

    public final String id;
    public final String ammoId;
    public final int capacity;
    public final float totalDamage;
    public final int pellets;
    public final int cooldown;
    public final float spread;
    public final float recoilAngle;
    public final int reloadTimer;
    public final int reloadExtra;
    public final boolean manualReload; // true — набої завантажуються по одному
    public final int range;

    GunType(String id, String ammoId, int capacity, float totalDamage, int pellets, int cooldown, float spread,
            float recoilAngle, int reloadTimer, int reloadExtra, boolean manualReload, int range) {
        this.id = id;
        this.ammoId = ammoId;
        this.capacity = capacity;
        this.totalDamage = totalDamage;
        this.pellets = pellets;
        this.cooldown = cooldown;
        this.spread = spread;
        this.recoilAngle = recoilAngle;
        this.reloadTimer = reloadTimer;
        this.reloadExtra = reloadExtra;
        this.manualReload = manualReload;
        this.range = range;
    }

    /** Шкода однієї кулі/дробини (як в оригіналі: повна шкода ділиться на кількість снарядів). */
    public float pelletDamage() {
        return totalDamage / pellets;
    }

    /** Час перезарядки в тіках для заданої кількості відсутніх набоїв. */
    public int reloadTicks(int missing) {
        return manualReload ? reloadTimer * missing + reloadExtra : reloadTimer + reloadExtra;
    }
}
