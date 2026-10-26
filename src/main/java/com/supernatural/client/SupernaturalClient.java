package com.supernatural.client;

import com.supernatural.block.ModBlocks;
import com.supernatural.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class SupernaturalClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.GHOST, GhostRenderer::new);
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SALT_LINE, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DEVILS_TRAP, RenderLayer.getCutout());
    }
}
