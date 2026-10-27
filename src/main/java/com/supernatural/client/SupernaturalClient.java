package com.supernatural.client;

import com.supernatural.block.ModBlocks;
import com.supernatural.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;

public class SupernaturalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.GHOST, GhostRenderer::new);
        Block[] cutout = {
                ModBlocks.SALT_CRYSTAL, ModBlocks.SALT_LINE, ModBlocks.DEVILS_TRAP,
                ModBlocks.TOMBSTONE_NORMAL, ModBlocks.TOMBSTONE_OLD, ModBlocks.TOMBSTONE_CURSED,
                ModBlocks.TOMBSTONE_ANCIENT, ModBlocks.TOMBSTONE_PEACEFUL,
                ModBlocks.REMAINS_NORMAL, ModBlocks.REMAINS_EVIL, ModBlocks.REMAINS_STRONG,
                ModBlocks.REMAINS_ANCIENT, ModBlocks.REMAINS_CURSED, ModBlocks.REMAINS_PEACEFUL,
                ModBlocks.BURIAL_URN};
        for (Block b : cutout) {
            BlockRenderLayerMap.INSTANCE.putBlock(b, RenderLayer.getCutout());
        }
    }
}
