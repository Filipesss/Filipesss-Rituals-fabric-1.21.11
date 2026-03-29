package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public class PulseBlasterBeamRenderer
        extends EntityRenderer<PulseBlasterBeamEntity, PulseBlasterBeamRenderer.BeamRenderState> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath("rituals", "textures/entity/pulse_blaster_beam.png");

    private static final float MODEL_CENTER_Y = 1.266f;

    private final PulseBlasterBeamModel model;

    public PulseBlasterBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PulseBlasterBeamModel(context.bakeLayer(PulseBlasterBeamModel.LAYER));
    }

    // -------------------------------------------------------------------------
    // Render state
    // -------------------------------------------------------------------------

    public static class BeamRenderState extends EntityRenderState {
        public boolean hasVelocity = false;
        public float yaw   = 0f;
        public float pitch = 0f;
    }

    @Override
    public BeamRenderState createRenderState() {
        return new BeamRenderState();
    }

    @Override
    public void extractRenderState(PulseBlasterBeamEntity entity,
                                   BeamRenderState state,
                                   float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Vec3 vel = entity.getDeltaMovement();
        double lenSq = vel.x * vel.x + vel.y * vel.y + vel.z * vel.z;

        if (lenSq > 0.0001) {
            state.hasVelocity = true;
            state.yaw   = (float) Math.toDegrees(Math.atan2(-vel.x, vel.z));
            state.pitch = (float) Math.toDegrees(
                    Math.atan2(-vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z))
            );
        } else {
            state.hasVelocity = false;
        }
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void submit(BeamRenderState state,
                       PoseStack poseStack,
                       SubmitNodeCollector submitNodeCollector,
                       CameraRenderState camera) {
        if (!state.hasVelocity) return;

        MultiBufferSource.BufferSource bufferSource =
                Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.translate(0f, -MODEL_CENTER_Y, 0f);

        this.model.getBone().render(
                poseStack,
                bufferSource.getBuffer(RenderTypes.entityTranslucentEmissive(TEXTURE)),
                state.lightCoords,
                OverlayTexture.NO_OVERLAY
        );

        bufferSource.endBatch();
        poseStack.popPose();
    }
}