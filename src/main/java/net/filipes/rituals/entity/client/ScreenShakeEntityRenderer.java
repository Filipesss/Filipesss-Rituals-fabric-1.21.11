package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class ScreenShakeEntityRenderer
        extends EntityRenderer<ScreenShakeEntity, ScreenShakeEntityRenderer.ScreenShakeRenderState> {

    public static class ScreenShakeRenderState extends EntityRenderState {
    }

    public ScreenShakeEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ScreenShakeRenderState createRenderState() {
        return new ScreenShakeRenderState();
    }

    @Override
    public boolean shouldRender(ScreenShakeEntity entity, Frustum frustum, double x, double y, double z) {
        return false;
    }

    @Override
    protected float getShadowRadius(ScreenShakeRenderState state) {
        return 0.0f;
    }

    @Override
    protected float getShadowStrength(ScreenShakeRenderState state) {
        return 0.0f;
    }

    @Override
    public void submit(ScreenShakeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    }
}