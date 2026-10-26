package com.supernatural.entity;

import com.supernatural.SupernaturalMod;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;

public class ModEntities {
    public static final EntityType<GhostEntity> GHOST = Registry.register(Registries.ENTITY_TYPE,
            SupernaturalMod.id("ghost"),
            EntityType.Builder.create(GhostEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 1.95f).maxTrackingRange(8)
                    .build(SupernaturalMod.MOD_ID + ":ghost"));

    public static void register() {
        FabricDefaultAttributeRegistry.register(GHOST,
                ZombieEntity.createZombieAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0));
        SpawnRestriction.register(GHOST, SpawnLocation.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, HostileEntity::canSpawnInDark);
        // Рідкісний природний спавн. Пізніше прив'яжемо до покинутих будинків і цвинтарів.
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(), SpawnGroup.MONSTER, GHOST, 4, 1, 1);
    }
}
