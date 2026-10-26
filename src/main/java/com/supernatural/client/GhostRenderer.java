package com.supernatural.client;

import com.supernatural.SupernaturalMod;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

public class GhostRenderer extends ZombieEntityRenderer {
    private static final Identifier TEXTURE = SupernaturalMod.id("textures/entity/ghost.png");

    public GhostRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(ZombieEntity entity) {
        return TEXTURE;
    }
}
