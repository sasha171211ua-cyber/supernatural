package com.supernatural.entity;

import com.supernatural.block.ModBlocks;
import com.supernatural.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GhostEntity extends ZombieEntity {
    public GhostEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    // Шкода тільки від залізної кочерги (плюс /kill і порожнеча)
    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean poker = source.getAttacker() instanceof LivingEntity a
                && a.getMainHandStack().isOf(ModItems.IRON_POKER);
        if (poker) {
            amount *= 2.0f;
        } else if (!source.isOf(DamageTypes.GENERIC_KILL) && !source.isOf(DamageTypes.OUT_OF_WORLD)) {
            return false;
        }
        return super.damage(source, amount);
    }

    // Проходження крізь блоки, лише коли треба дістатися до цілі
    @Override
    public void tick() {
        if (!getWorld().isClient) {
            LivingEntity target = getTarget();
            boolean hasTarget = target != null && target.isAlive();
            boolean inBlocks = !getWorld().isSpaceEmpty(this);
            boolean phase = inBlocks || (hasTarget && !canSee(target));
            this.noClip = phase;
            setNoGravity(phase);
            if (phase) {
                Vec3d v = hasTarget
                        ? target.getPos().add(0, target.getHeight() / 2, 0)
                                .subtract(getPos().add(0, getHeight() / 2, 0))
                        : new Vec3d(0, 1, 0);
                if (v.lengthSquared() > 1.0E-4) {
                    setVelocity(v.normalize().multiply(0.15));
                }
            }
        }
        super.tick();
    }

    // Невидимий бар'єр від солі: тільки для привида
    @Override
    public void move(MovementType type, Vec3d movement) {
        if ((movement.x != 0 || movement.z != 0) && !touchesSalt(getBoundingBox())) {
            if (touchesSalt(getBoundingBox().offset(movement.x, 0, movement.z))) {
                Vec3d mx = new Vec3d(movement.x, movement.y, 0);
                Vec3d mz = new Vec3d(0, movement.y, movement.z);
                if (!touchesSalt(getBoundingBox().offset(mx))) {
                    movement = mx;
                } else if (!touchesSalt(getBoundingBox().offset(mz))) {
                    movement = mz;
                } else {
                    movement = new Vec3d(0, movement.y, 0);
                }
            }
        }
        super.move(type, movement);
    }

    private boolean touchesSalt(Box box) {
        Box b = box.contract(1.0E-3, 0, 1.0E-3);
        BlockPos.Mutable p = new BlockPos.Mutable();
        for (int x = MathHelper.floor(b.minX); x <= MathHelper.floor(b.maxX); x++) {
            for (int z = MathHelper.floor(b.minZ); z <= MathHelper.floor(b.maxZ); z++) {
                for (int y = MathHelper.floor(b.minY) - 1; y <= MathHelper.floor(b.maxY); y++) {
                    p.set(x, y, z);
                    if (getWorld().getBlockState(p).isOf(ModBlocks.SALT_LINE)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
