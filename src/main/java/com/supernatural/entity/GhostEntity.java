package com.supernatural.entity;

import com.supernatural.ghost.GhostType;
import com.supernatural.grave.GraveManager;
import com.supernatural.grave.HuntRules;
import com.supernatural.item.ModItems;
import com.supernatural.util.LightEffects;
import com.supernatural.util.SaltBarrier;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Привид. Один зареєстрований тип сутності, шість видів (GhostType) через синхронізоване поле.
 * Сервер — джерело істини для стану; клієнт лише малює.
 */
public class GhostEntity extends HostileEntity {
    private static final TrackedData<Integer> TYPE =
            DataTracker.registerData(GhostEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public enum Mode { IDLE, CHASE, RETREAT, WAIT, RETURN, DYING }

    private Mode mode = Mode.IDLE;
    private int hits;
    private int modeTimer;
    private int attackCd;
    private int abilityCd = 60;
    private int boostTicks;
    private int flickerCd = 80;
    private int stuckCount;
    private double lastHomeDist = Double.MAX_VALUE;
    private long lastHitTime = -100;
    private boolean hasGrave;
    private boolean illusion;
    private boolean registered;
    private int illusionLife;
    private long graveKey;
    private BlockPos homePos = BlockPos.ORIGIN;
    private BlockPos restPos = BlockPos.ORIGIN;
    private UUID targetId;
    private UUID rewardTo;

    public GhostEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        setPersistent();
        setNoGravity(true);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(TYPE, 0);
    }

    public GhostType getGhostType() {
        return GhostType.byOrdinal(dataTracker.get(TYPE));
    }

    public void setGhostType(GhostType t) {
        dataTracker.set(TYPE, t.ordinal());
        if (getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(t.health);
        }
        setHealth((float) t.health);
    }

    public void bindGrave(long key, BlockPos remains, BlockPos rest) {
        this.hasGrave = true;
        this.graveKey = key;
        this.homePos = remains;
        this.restPos = rest;
    }

    public void makeIllusion(int life) {
        this.illusion = true;
        this.illusionLife = life;
    }

    @Nullable
    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
                                 @Nullable EntityData entityData) {
        if (spawnReason == SpawnReason.SPAWN_EGG) {
            setGhostType(GhostType.byOrdinal(random.nextInt(GhostType.values().length)));
        }
        homePos = getBlockPos();
        restPos = getBlockPos();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    // ---------- збереження ----------
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("GhostType", getGhostType().ordinal());
        nbt.putInt("Hits", hits);
        nbt.putString("Mode", mode.name());
        nbt.putInt("Timer", modeTimer);
        nbt.putBoolean("HasGrave", hasGrave);
        nbt.putLong("GraveKey", graveKey);
        nbt.putLong("Home", homePos.asLong());
        nbt.putLong("Rest", restPos.asLong());
        nbt.putBoolean("Illusion", illusion);
        nbt.putInt("IllusionLife", illusionLife);
        if (targetId != null) nbt.putUuid("Target", targetId);
        if (rewardTo != null) nbt.putUuid("RewardTo", rewardTo);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("GhostType")) setGhostType(GhostType.byOrdinal(nbt.getInt("GhostType")));
        else if (nbt.contains("Type")) setGhostType(GhostType.byId(nbt.getString("Type")));
        hits = nbt.getInt("Hits");
        try {
            mode = Mode.valueOf(nbt.getString("Mode"));
        } catch (IllegalArgumentException e) {
            mode = Mode.IDLE;
        }
        modeTimer = nbt.getInt("Timer");
        hasGrave = nbt.getBoolean("HasGrave");
        graveKey = nbt.getLong("GraveKey");
        if (nbt.contains("Home")) homePos = BlockPos.fromLong(nbt.getLong("Home"));
        if (nbt.contains("Rest")) restPos = BlockPos.fromLong(nbt.getLong("Rest"));
        illusion = nbt.getBoolean("Illusion");
        illusionLife = nbt.getInt("IllusionLife");
        if (nbt.containsUuid("Target")) targetId = nbt.getUuid("Target");
        if (nbt.containsUuid("RewardTo")) rewardTo = nbt.getUuid("RewardTo");
    }

    // ---------- звуки / налаштування ----------
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VEX_DEATH;
    }

    @Override
    protected boolean isDisallowedInPeaceful() {
        return false;
    }

    // ---------- шкода: лише залізна кочерга ----------
    @Override
    public boolean damage(DamageSource source, float amount) {
        if (getWorld().isClient) return false;
        if (source.isOf(DamageTypes.GENERIC_KILL) || source.isOf(DamageTypes.OUT_OF_WORLD)) {
            return super.damage(source, amount); // лише /kill та порожнеча
        }
        if (!(source.getAttacker() instanceof ServerPlayerEntity p)
                || source.getSource() != p
                || !p.getMainHandStack().isOf(ModItems.IRON_POKER)) {
            return false;
        }
        if (mode == Mode.DYING) return false;
        ServerWorld sw = (ServerWorld) getWorld();
        GhostType t = getGhostType();
        if (illusion) { // клон зникає, лічильник не рахується
            sw.spawnParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 12, 0.3, 0.5, 0.3, 0.02);
            discard();
            return true;
        }
        if (!t.hostile() || mode != Mode.CHASE) return false;
        long now = sw.getTime();
        if (now - lastHitTime < 14) return false; // ~0.7 с: захист від повторної обробки удару
        lastHitTime = now;
        hits++;
        takeKnockback(0.15, p.getX() - getX(), p.getZ() - getZ());
        setHealth(Math.max(1.0f, getMaxHealth() * (1.0f - hits / (float) t.hitsToRetreat)));
                sw.sendEntityStatus(this, (byte) 2);
        playSound(SoundEvents.ENTITY_VEX_HURT, 1.0f, 0.8f);
        if (hits >= t.hitsToRetreat) {
            mode = Mode.RETREAT;
            modeTimer = 0;
            lastHomeDist = Double.MAX_VALUE;
            stuckCount = 0;
            setAttacking(false);
        }
        return true;
    }

    // ---------- бар'єр солі ----------
    @Override
    public void move(MovementType type, Vec3d movement) {
        if (!getWorld().isClient && mode != Mode.DYING && movement.lengthSquared() > 1.0E-8) {
            Vec3d from = getPos();
            if (SaltBarrier.crosses(getWorld(), from, from.add(movement))) {
                Vec3d mx = new Vec3d(movement.x, movement.y, 0);
                Vec3d mz = new Vec3d(0, movement.y, movement.z);
                if (!SaltBarrier.crosses(getWorld(), from, from.add(mx))) {
                    movement = mx;
                } else if (!SaltBarrier.crosses(getWorld(), from, from.add(mz))) {
                    movement = mz;
                } else {
                    movement = new Vec3d(0, movement.y, 0);
                    if (SaltBarrier.crosses(getWorld(), from, from.add(movement))) movement = Vec3d.ZERO;
                }
            }
        }
        super.move(type, movement);
    }

    // ---------- головний цикл ----------
    @Override
    public void tick() {
        if (!getWorld().isClient && getWorld() instanceof ServerWorld sw) {
            serverTick(sw);
        }
        super.tick();
    }

    private void serverTick(ServerWorld sw) {
        GhostType t = getGhostType();
        if (attackCd > 0) attackCd--;
        if (abilityCd > 0) abilityCd--;
        if (boostTicks > 0) boostTicks--;

        if (!registered) {
            registered = true;
            if (hasGrave && !GraveManager.claim(sw, graveKey, getUuid())) {
                discard(); // дублікат одного захоронення
                return;
            }
        }
        if (illusion) {
            setVelocity(Vec3d.ZERO);
            if (--illusionLife <= 0) {
                sw.spawnParticles(ParticleTypes.POOF, getX(), getY() + 1, getZ(), 10, 0.3, 0.5, 0.3, 0.02);
                discard();
            }
            return;
        }
        if (hasGrave && age % 20 == 0) GraveManager.touch(sw, graveKey, getUuid());

        ambient(sw, t);
        if (mode != Mode.DYING && t.hostile() && --flickerCd <= 0) {
            flickerCd = 20 * (t.flickerMinSec + random.nextInt(t.flickerMaxSec - t.flickerMinSec + 1));
            if (sw.getClosestPlayer(this, 48.0) != null) LightEffects.run(sw, this, t, false);
        }

        switch (mode) {
            case IDLE -> idle(sw, t);
            case CHASE -> chase(sw, t);
            case RETREAT -> goHome(sw, 0.8, true);
            case RETURN -> goHome(sw, 0.5, false);
            case WAIT -> {
                noClip = true;
                setVelocity(Vec3d.ZERO);
                if (--modeTimer <= 0) resume(sw);
            }
            case DYING -> dying(sw, t);
        }
    }

    private void ambient(ServerWorld sw, GhostType t) {
        if (age % 6 != 0) return;
        ParticleEffect p = switch (t) {
            case EVIL -> ParticleTypes.SMOKE;
            case ANCIENT -> ParticleTypes.ENCHANT;
            case CURSED -> ParticleTypes.SQUID_INK;
            case PEACEFUL -> ParticleTypes.END_ROD;
            default -> ParticleTypes.SOUL;
        };
        sw.spawnParticles(p, getX(), getY() + 1.0, getZ(), 1, 0.25, 0.5, 0.25, 0.01);
    }

    // ---------- стани ----------
    private void idle(ServerWorld sw, GhostType t) {
        setAttacking(false);
        noClip = !sw.isSpaceEmpty(this);
        Vec3d rest = Vec3d.ofCenter(restPos);
        Vec3d d = rest.subtract(getPos().add(0, getHeight() * 0.5, 0));
        double bob = Math.sin(age * 0.1) * 0.01;
        double sp = t == GhostType.PEACEFUL ? 0.04 : 0.05;
        if (d.length() > 1.5) setVelocity(d.normalize().multiply(sp).add(0, bob, 0));
        else setVelocity(new Vec3d(0, bob, 0));

        if (!t.hostile()) {
            peacefulHints(sw);
            return;
        }
        if (age % 10 == 0) {
            PlayerEntity pl = sw.getClosestPlayer(getX(), getY(), getZ(), t.detect,
                    e -> e instanceof ServerPlayerEntity sp2 && sp2.isAlive() && !sp2.isSpectator()
                            && HuntRules.isActive(sp2));
            if (pl != null) {
                targetId = pl.getUuid();
                mode = Mode.CHASE;
                attackCd = 20;
            }
        }
    }

    private void peacefulHints(ServerWorld sw) {
        PlayerEntity pl = sw.getClosestPlayer(this, 24.0);
        if (pl == null || age % 30 != 0) return;
        Vec3d from = getPos().add(0, 1.0, 0);
        Vec3d to = Vec3d.ofCenter(homePos);
        for (int i = 1; i <= 6; i++) {
            Vec3d p = from.lerp(to, i / 6.0);
            sw.spawnParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        float yaw = (float) (MathHelper.atan2(pl.getZ() - getZ(), pl.getX() - getX()) * 57.29577951308232) - 90.0f;
        setYaw(yaw);
        setHeadYaw(yaw);
    }

    @Nullable
    private ServerPlayerEntity targetPlayer(ServerWorld sw) {
        if (targetId == null) return null;
        ServerPlayerEntity p = sw.getServer().getPlayerManager().getPlayer(targetId);
        if (p == null || !p.isAlive() || p.isSpectator() || p.getServerWorld() != sw) return null;
        return p;
    }

    private void chase(ServerWorld sw, GhostType t) {
        ServerPlayerEntity p = targetPlayer(sw);
        if (p == null || !HuntRules.isActive(p)) { // гравець загинув / пішов / інший вимір
            targetId = null;
            setAttacking(false);
            mode = Mode.RETURN;
            return;
        }
        Vec3d to = p.getPos().add(0, p.getHeight() * 0.5, 0);
        Vec3d from = getPos().add(0, getHeight() * 0.5, 0);
        Vec3d d = to.subtract(from);
        double dist = d.length();

        if (dist > 90 && age % 40 == 0) catchUp(sw, p, d); // переслідує на будь-якій відстані без підвантаження чанків

        boolean inside = !sw.isSpaceEmpty(this);
        boolean los = canSee(p);
        noClip = inside || !los;

        double speed = t.speed * (boostTicks > 0 ? 1.4 : 1.0);
        if (dist > 1.6) setVelocity(d.normalize().multiply(Math.min(speed, dist - 1.2)));
        else setVelocity(Vec3d.ZERO);
        face(d);
        setAttacking(dist < 8);

        if (dist < 2.4 && los && !noClip && attackCd <= 0) {
            p.damage(sw.getDamageSources().mobAttack(this), (float) t.damage);
            swingHand(Hand.MAIN_HAND);
            attackCd = t.attackCooldown;
        }
        abilities(sw, t, p, dist);
    }

    private void face(Vec3d d) {
        float yaw = (float) (MathHelper.atan2(d.z, d.x) * 57.29577951308232) - 90.0f;
        setYaw(yaw);
        setHeadYaw(yaw);
        setBodyYaw(yaw);
    }

    private void catchUp(ServerWorld sw, ServerPlayerEntity p, Vec3d d) {
        Vec3d dir = new Vec3d(-d.x, 0, -d.z).normalize(); // від гравця в бік привида
        for (int i = 0; i < 6; i++) {
            double r = 40 + random.nextInt(12);
            Vec3d c = p.getPos().add(dir.multiply(r)).add((random.nextDouble() - 0.5) * 10, 0, (random.nextDouble() - 0.5) * 10);
            BlockPos bp = BlockPos.ofFloored(c.x, p.getY() + 2, c.z);
            if (!sw.isChunkLoaded(bp)) continue;
            if (!safe(sw, bp)) continue;
            refreshPositionAndAngles(c.x, bp.getY(), c.z, getYaw(), 0);
            return;
        }
    }

    private boolean safe(ServerWorld sw, BlockPos bp) {
        return sw.isChunkLoaded(bp)
                && sw.getBlockState(bp).getCollisionShape(sw, bp).isEmpty()
                && sw.getBlockState(bp.up()).getCollisionShape(sw, bp.up()).isEmpty()
                && !SaltBarrier.columnHasSalt(sw, bp.getX(), bp.getY(), bp.getZ());
    }

    private void goHome(ServerWorld sw, double speed, boolean glow) {
        noClip = true;
        setAttacking(false);
        modeTimer++;
        if (glow && modeTimer < 40) { // «слід»: світіння видно крізь стіни максимум ~2 с
            addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 6, 0, false, false));
        }
        Vec3d dest = Vec3d.ofCenter(homePos);
        Vec3d d = dest.subtract(getPos().add(0, getHeight() * 0.5, 0));
        double dist = d.length();
        if (dist < 1.0) {
            setVelocity(Vec3d.ZERO);
            if (glow) {
                mode = Mode.WAIT;
                modeTimer = 100 + random.nextInt(61); // 5–8 с
            } else {
                mode = Mode.IDLE;
            }
            return;
        }
        setVelocity(d.normalize().multiply(Math.min(speed, dist)));
        face(d);
        if (glow && modeTimer < 60) {
            sw.spawnParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1.0, getZ(), 2, 0.1, 0.1, 0.1, 0.01);
        }
        if (age % 20 == 0) { // захист від зависання (наприклад, сіль на шляху)
            if (lastHomeDist - dist < 0.5) stuckCount++; else stuckCount = 0;
            lastHomeDist = dist;
            if (stuckCount >= 3) {
                refreshPositionAndAngles(dest.x, dest.y - getHeight() * 0.5, dest.z, getYaw(), 0);
                stuckCount = 0;
            }
        }
    }

    /** Після відступу: повертається до гравця, затримка 0.8 с перед атакою. */
    private void resume(ServerWorld sw) {
        ServerPlayerEntity p = targetPlayer(sw);
        if (p == null) {
            mode = Mode.IDLE;
            return;
        }
        for (int i = 0; i < 12; i++) {
            double ang = random.nextDouble() * Math.PI * 2;
            double r = 4 + random.nextDouble() * 3;
            Vec3d c = p.getPos().add(Math.cos(ang) * r, 0, Math.sin(ang) * r);
            BlockPos bp = BlockPos.ofFloored(c.x, p.getY(), c.z);
            if (!safe(sw, bp)) continue;
            if (SaltBarrier.crosses(sw, p.getPos(), c)) continue; // не з'являтися за лінією солі
            refreshPositionAndAngles(c.x, bp.getY(), c.z, getYaw(), 0);
            sw.spawnParticles(ParticleTypes.SOUL, c.x, bp.getY() + 1, c.z, 15, 0.3, 0.6, 0.3, 0.02);
            hits = 0;
            setHealth(getMaxHealth());
            mode = Mode.CHASE;
            attackCd = 16; // 0.8 с
            return;
        }
        modeTimer = 20; // повторити спробу пізніше
    }

    // ---------- здібності ----------
    private void abilities(ServerWorld sw, GhostType t, ServerPlayerEntity p, double dist) {
        if (age % 10 != 0 || abilityCd > 0) return;
        switch (t) {
            case EVIL -> {
                if (dist > 5 && random.nextFloat() < 0.25f) {
                    boostTicks = 40;
                    abilityCd = 160;
                }
            }
            case STRONG -> {
                if (dist > 5 && dist < 30 && random.nextFloat() < 0.60f) {
                    LightEffects.run(sw, this, t, false);
                    if (teleportBehind(sw, p)) {
                        abilityCd = 160 + random.nextInt(81);
                        attackCd = Math.max(attackCd, 12);
                    } else {
                        abilityCd = 40;
                    }
                }
            }
            case ANCIENT -> {
                if (dist > 4 && dist < 40) {
                    LightEffects.run(sw, this, t, false);
                    if (teleportBehind(sw, p)) {
                        abilityCd = 120 + random.nextInt(81);
                        attackCd = Math.max(attackCd, 8 + random.nextInt(7));
                        if (random.nextFloat() < 0.25f) spawnIllusion(sw, p);
                    } else {
                        abilityCd = 40;
                    }
                }
            }
            case CURSED -> {
                if (dist > 4 && dist < 40) {
                    if (teleportBehind(sw, p)) {
                        abilityCd = 200 + random.nextInt(81);
                        attackCd = Math.max(attackCd, 10);
                        p.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 20 + random.nextInt(21), 0, false, false));
                        if (random.nextFloat() < 0.30f) {
                            p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 20, 0, false, false));
                            LightEffects.run(sw, this, t, true);
                        }
                    } else {
                        abilityCd = 40;
                    }
                }
            }
            default -> { }
        }
    }

    private boolean teleportBehind(ServerWorld sw, ServerPlayerEntity p) {
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d flat = new Vec3d(look.x, 0, look.z);
        flat = flat.lengthSquared() < 1.0E-4 ? new Vec3d(0, 0, 1) : flat.normalize();
        for (int i = 0; i < 8; i++) {
            double ang = (random.nextDouble() - 0.5) * 0.8;
            double cos = Math.cos(ang), sin = Math.sin(ang);
            Vec3d dir = new Vec3d(flat.x * cos - flat.z * sin, 0, flat.x * sin + flat.z * cos);
            double r = 2 + random.nextDouble() * 2;
            Vec3d c = p.getPos().subtract(dir.multiply(r));
            BlockPos bp = BlockPos.ofFloored(c.x, p.getY(), c.z);
            if (!safe(sw, bp)) continue;
            if (SaltBarrier.crosses(sw, getPos(), c)) continue;
            sw.spawnParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 20, 0.3, 0.6, 0.3, 0.2);
            refreshPositionAndAngles(c.x, bp.getY(), c.z, getYaw(), 0);
            sw.spawnParticles(ParticleTypes.PORTAL, c.x, bp.getY() + 1, c.z, 20, 0.3, 0.6, 0.3, 0.2);
            playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, 0.6f, 0.6f);
            return true;
        }
        return false;
    }

    private void spawnIllusion(ServerWorld sw, ServerPlayerEntity p) {
        GhostEntity c = ModEntities.GHOST.create(sw);
        if (c == null) return;
        c.setGhostType(getGhostType());
        c.makeIllusion(40); // 2 с
        double ang = random.nextDouble() * Math.PI * 2;
        Vec3d pos = p.getPos().add(Math.cos(ang) * 4, 0, Math.sin(ang) * 4);
        BlockPos bp = BlockPos.ofFloored(pos.x, p.getY(), pos.z);
        if (!safe(sw, bp) || SaltBarrier.crosses(sw, p.getPos(), pos)) return;
        c.refreshPositionAndAngles(pos.x, bp.getY(), pos.z, getYaw(), 0);
        sw.spawnEntity(c);
    }

    // ---------- остаточне знищення ----------
    public void beginDestruction(ServerPlayerEntity p) {
        mode = Mode.DYING;
        modeTimer = 0;
        rewardTo = p.getUuid();
        setAttacking(false);
        setVelocity(Vec3d.ZERO);
        noClip = true;
        Vec3d look = p.getRotationVec(1.0f);
        Vec3d flat = new Vec3d(look.x, 0, look.z);
        flat = flat.lengthSquared() < 1.0E-4 ? new Vec3d(0, 0, 1) : flat.normalize().multiply(2.5);
        refreshPositionAndAngles(p.getX() + flat.x, p.getY(), p.getZ() + flat.z, getYaw(), 0);
        face(new Vec3d(-flat.x, 0, -flat.z));
        playSound(getGhostType().hostile() ? SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE : SoundEvents.ENTITY_VEX_AMBIENT, 1.0f, 0.7f);
    }

    private void dying(ServerWorld sw, GhostType t) {
        noClip = true;
        setVelocity(Vec3d.ZERO);
        modeTimer++;
        ParticleEffect fx = t.hostile() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.END_ROD;
        if (modeTimer % 2 == 0) {
            sw.spawnParticles(fx, getX(), getY() + 1.0, getZ(), 6, 0.3, 0.7, 0.3, 0.03);
        }
        if (modeTimer >= 80) { // ~4 с
            sw.playSound(null, getBlockPos(), SoundEvents.ENTITY_VEX_DEATH, net.minecraft.sound.SoundCategory.HOSTILE, 1.0f, 0.6f);
            GraveManager.grantRewards(sw, t, getPos());
            discard();
        }
    }
}
