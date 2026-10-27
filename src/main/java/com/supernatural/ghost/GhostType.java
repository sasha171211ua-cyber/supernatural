package com.supernatural.ghost;

public enum GhostType {
    //            id         hp  dmg  speed detect hits cd   ectoMin ectoMax trophy% flickMin flickMax (сек)
    NORMAL("normal",     20, 3, 0.25, 30,  6, 30, 2, 4,  2, 4, 7),
    EVIL("evil",         26, 4, 0.28, 40,  7, 28, 4, 6,  5, 3, 6),
    STRONG("strong",     34, 5, 0.30, 55,  9, 26, 6, 9, 10, 2, 5),
    ANCIENT("ancient",   44, 6, 0.32, 100, 10, 24, 10, 15, 25, 1, 4),
    CURSED("cursed",     30, 4, 0.27, 65,  7, 28, 5, 8, 15, 2, 4),
    PEACEFUL("peaceful", 20, 0, 0.08, 20,  0, 0, 0, 0,  0, 0, 0);

    public final String id;
    public final double health;
    public final double damage;
    public final double speed;
    public final double detect;
    public final int hitsToRetreat;
    public final int attackCooldown;
    public final int ectoMin, ectoMax;
    public final int trophyChance;
    public final int flickerMinSec, flickerMaxSec;

    GhostType(String id, double health, double damage, double speed, double detect, int hits, int cd,
              int ectoMin, int ectoMax, int trophy, int fMin, int fMax) {
        this.id = id;
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        this.detect = detect;
        this.hitsToRetreat = hits;
        this.attackCooldown = cd;
        this.ectoMin = ectoMin;
        this.ectoMax = ectoMax;
        this.trophyChance = trophy;
        this.flickerMinSec = fMin;
        this.flickerMaxSec = fMax;
    }

    public boolean hostile() {
        return this != PEACEFUL;
    }

    public static GhostType byOrdinal(int i) {
        GhostType[] v = values();
        return v[Math.floorMod(i, v.length)];
    }

    public static GhostType byId(String s) {
        for (GhostType t : values()) {
            if (t.id.equals(s)) return t;
        }
        return NORMAL;
    }
}
