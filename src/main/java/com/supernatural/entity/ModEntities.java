package com.supernatural.entity;

import com.supernatural.SupernaturalMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
    public static final EntityType<GhostEntity> GHOST = Registry.register(Registries.ENTITY_TYPE,
            SupernaturalMod.id("ghost"),
            EntityType.Builder.create(GhostEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).maxTrackingRange(10).makeFireImmune()
                    .build(SupernaturalMod.MOD_ID + ":ghost"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(GHOST, HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 64.0));
        // Природного спавну немає: привиди з'являються лише з захоронень (та з яйця виклику).
    }
}
