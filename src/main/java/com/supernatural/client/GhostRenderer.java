package com.supernatural.client;

import com.supernatural.SupernaturalMod;
import com.supernatural.entity.GhostEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.ZombieEntityModel;
import net.minecraft.util.Identifier;

public class GhostRenderer extends BipedEntityRenderer<GhostEntity, ZombieEntityModel<GhostEntity>> {
    public GhostRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ZombieEntityModel<>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.4f);
    }

    @Override
    public Identifier getTexture(GhostEntity entity) {
        return SupernaturalMod.id("textures/entity/ghost_" + entity.getGhostType().id + ".png");
    }

    @Override
    protected RenderLayer getRenderLayer(GhostEntity entity, boolean showBody, boolean translucent, boolean showOutline) {
        if (showOutline) return super.getRenderLayer(entity, showBody, translucent, showOutline);
        return RenderLayer.getEntityTranslucent(getTexture(entity));
    }
}
