package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.SparkEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class SparkEntityRenderer extends EntityRenderer<SparkEntity, SparkEntityRenderer.SparkRenderState> {


    public static class SparkRenderState extends EntityRenderState {
        float relX, relY, relZ;
        final List<Vec3> trail = new ArrayList<>();

        int   glowR, glowG, glowB;
        int   coreR, coreG, coreB;
        float glowWidth, coreWidth;
        int   peakAlpha;
    }


    public SparkEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public SparkRenderState createRenderState() { return new SparkRenderState(); }

    @Override
    public void extractRenderState(SparkEntity e, SparkRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;
        s.relX = (float)(eX - s.x);
        s.relY = (float)(eY - s.y);
        s.relZ = (float)(eZ - s.z);
        s.trail.clear();
        s.trail.add(new Vec3(eX, eY, eZ));
        s.trail.addAll(e.trailPositions);

        s.glowR = e.trailGlowR; s.glowG = e.trailGlowG; s.glowB = e.trailGlowB;
        s.coreR = e.trailCoreR; s.coreG = e.trailCoreG; s.coreB = e.trailCoreB;
        s.glowWidth  = e.trailGlowWidth;
        s.coreWidth  = e.trailCoreWidth;
        s.peakAlpha  = e.trailAlpha;
    }

    @Override public boolean affectedByCulling(SparkEntity e) { return false; }
    @Override protected float getShadowRadius  (SparkRenderState s) { return 0f; }
    @Override protected float getShadowStrength(SparkRenderState s) { return 0f; }

    @Override
    public void submit(SparkRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        List<Vec3> trail = s.trail;
        if (trail.size() < 2) return;

        final int   total     = trail.size();
        final float glowW     = s.glowWidth;
        final float coreW     = s.coreWidth;
        final int   glowR     = s.glowR, glowG = s.glowG, glowB = s.glowB;
        final int   coreR     = s.coreR, coreG = s.coreG, coreB = s.coreB;
        final int   peakAlpha = s.peakAlpha;
        final double camX = s.x, camY = s.y, camZ = s.z;

        ps.pushPose();

        for (int i = 0; i < total - 1; i++) {
            Vec3 a = trail.get(i);
            Vec3 b = trail.get(i + 1);
            Vec3 ra = new Vec3(a.x - camX, a.y - camY, a.z - camZ);
            Vec3 rb = new Vec3(b.x - camX, b.y - camY, b.z - camZ);

            float t      = i / (float)(total - 1);
            float tNext  = (i + 1) / (float)(total - 1);
            int   alA    = (int)(peakAlpha * (1f - t));
            int   alB    = (int)(peakAlpha * (1f - tNext));

            int segAl = (alA + alB) / 2;
            if (segAl < 4) continue;

            Vec3 dir    = rb.subtract(ra);
            if (dir.lengthSqr() < 1e-8) continue;
            dir = dir.normalize();

            Vec3 helper = (Math.abs(dir.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 right  = dir.cross(helper).normalize();
            Vec3 up     = dir.cross(right).normalize();

            final Vec3 pA = ra, pB = rb;
            final Vec3 pRight = right, pUp = up;
            final int  gR = glowR, gG = glowG, gB = glowB;
            final int  cR = coreR, cG = coreG, cB = coreB;
            final float gW = glowW, cW = coreW;
            final int   al = segAl;

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                crossQuad(pose, v, pA, pB, pRight, gW, gR, gG, gB, al);
                crossQuad(pose, v, pA, pB, pUp,    gW, gR, gG, gB, al);
            });

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                crossQuad(pose, v, pA, pB, pRight, cW, cR, cG, cB, al);
                crossQuad(pose, v, pA, pB, pUp,    cW, cR, cG, cB, al);
            });
        }

        ps.popPose();
    }

    private static void crossQuad(PoseStack.Pose pose, VertexConsumer v,
                                  Vec3 a, Vec3 b, Vec3 axis, float w,
                                  int r, int g, int bl, int al) {
        float h   = w * 0.5f;
        Vec3  off = axis.scale(h);
        Vec3  a0  = a.add(off), a1 = a.subtract(off);
        Vec3  b0  = b.add(off), b1 = b.subtract(off);
        bv(pose, v, a0, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b0, r, g, bl, al);
        bv(pose, v, b1, r, g, bl, al);
        bv(pose, v, a1, r, g, bl, al);
        bv(pose, v, a0, r, g, bl, al);
    }

    private static void bv(PoseStack.Pose pose, VertexConsumer v,
                           Vec3 p, int r, int g, int b, int a) {
        v.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(r, g, b, a);
    }
}