package com.supernatural;

import com.supernatural.block.ModBlocks;
import com.supernatural.entity.ModEntities;
import com.supernatural.grave.GraveManager;
import com.supernatural.gun.GunNetworking;
import com.supernatural.gun.ModGuns;
import com.supernatural.item.ModItems;
import com.supernatural.world.GraveFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public class SupernaturalMod implements ModInitializer {
    public static final String MOD_ID = "supernatural";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static final Feature<DefaultFeatureConfig> GRAVE_FEATURE =
            Registry.register(Registries.FEATURE, id("grave"), new GraveFeature());

    private static RegistryKey<PlacedFeature> placed(String name) {
        return RegistryKey.of(RegistryKeys.PLACED_FEATURE, id(name));
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModEntities.register();
        ModItems.register();
        ModGuns.register();
        GunNetworking.register();

        // Сольові кристали: на стелі й підлозі підземель Верхнього світу
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION, placed("salt_crystal_floor"));
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION, placed("salt_crystal_ceiling"));

        // Могили: у підземеллях усього Верхнього світу
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_STRUCTURES, placed("grave_underground"));

        ServerChunkEvents.CHUNK_LOAD.register(GraveManager::onChunkLoad);
        ServerChunkEvents.CHUNK_UNLOAD.register(GraveManager::onChunkUnload);
        ServerTickEvents.END_WORLD_TICK.register(GraveManager::onWorldTick);
    }
}
