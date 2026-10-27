package com.supernatural.client;

import com.supernatural.entity.GhostEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

/** Двоногая модель як у зомбі; під час атаки руки піднімаються вперед. */
public class GhostModel extends BipedEntityModel<GhostEntity> {
    public GhostModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setAngles(GhostEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {
        super.setAngles(entity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
        if (entity.isAttacking()) {
            this.rightArm.pitch = -1.5f;
            this.leftArm.pitch = -1.5f;
            this.rightArm.yaw = 0.0f;
            this.leftArm.yaw = 0.0f;
        }
    }
}
