package com.supernatural.entity;

import com.supernatural.block.ModBlocks;
import com.supernatural.item.ModItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.world.World;
import net.minecraft.util.math.Vec3d;

/** Привид: не горить на сонці, слабкий до залізної кочерги й святої води, не проходить сіль. */
public class GhostEntity extends ZombieEntity {
    public GhostEntity(EntityType<? extends ZombieEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected boolean isAffectedByDaylight() {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        boolean poker = source.getAttacker() instanceof LivingEntity a
                && a.getMainHandStack().isOf(ModItems.IRON_POKER);
        if (poker) {
            amount *= 2.0f;
        } else if (!source.isOf(DamageTypes.MAGIC) && !source.isOf(DamageTypes.OUT_OF_WORLD)) {
            amount *= 0.4f; // звичайна зброя майже не шкодить
        }
        return super.damage(source, amount);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getWorld().isClient && age % 5 == 0) {
            if (getWorld().getBlockState(getBlockPos()).isIn(ModBlocks.WARDING)) {
                Vec3d push = getPos().subtract(Vec3d.ofCenter(getBlockPos())).normalize().multiply(0.7);
                setVelocity(push.x, 0.3, push.z);
                velocityModified = true;
                super.damage(getDamageSources().magic(), 2.0f);
            }
        }
    }
}
