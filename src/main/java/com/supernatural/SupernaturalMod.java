package com.supernatural;

import com.supernatural.block.ModBlocks;
import com.supernatural.entity.ModEntities;
import com.supernatural.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

public class SupernaturalMod implements ModInitializer {
    public static final String MOD_ID = "supernatural";

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModEntities.register();
        ModItems.register();

        // Соляна руда генерується в усіх біомах Верхнього світу
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_ORES,
                RegistryKey.of(RegistryKeys.PLACED_FEATURE, id("salt_ore")));
    }
}
