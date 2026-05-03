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
        final List<Vec3> trail = new ArrayList<>();
        int   r, g, b;
        float width;
        int   peakAlpha;
        int   windowOffset;
        int   windowSize;
    }

    public SparkEntityRenderer(EntityRendererProvider.Context ctx) { super(ctx); }

    @Override public SparkRenderState createRenderState() { return new SparkRenderState(); }

    @Override
    public void extractRenderState(SparkEntity e, SparkRenderState s, float pt) {
        super.extractRenderState(e, s, pt);

        double eX = e.xo + (e.getX() - e.xo) * pt;
        double eY = e.yo + (e.getY() - e.yo) * pt;
        double eZ = e.zo + (e.getZ() - e.zo) * pt;

        s.trail.clear();
        s.trail.add(new Vec3(eX, eY, eZ));
        s.trail.addAll(e.trailPositions);

        s.r          = e.trailR;
        s.g          = e.trailG;
        s.b          = e.trailB;
        s.width      = e.trailWidth;
        s.peakAlpha  = e.trailAlpha;
        s.windowOffset = e.trailWindowOffset;
        s.windowSize   = e.windowSize;
        s.trail.clear();
        s.trail.addAll(e.trailPositions);
        s.trail.add(new Vec3(eX, eY, eZ));
    }

    @Override public boolean affectedByCulling(SparkEntity e) { return false; }
    @Override protected float getShadowRadius  (SparkRenderState s) { return 0f; }
    @Override protected float getShadowStrength(SparkRenderState s) { return 0f; }

    @Override
    public void submit(SparkRenderState s, PoseStack ps,
                       SubmitNodeCollector snc, CameraRenderState cam) {

        List<Vec3> trail = s.trail;
        if (trail.size() < 2) return;

        final int    total  = trail.size();
        int numSeg = trail.size() - 1;

        int winEnd = Math.min(s.windowOffset, numSeg - 1);
        int winStart = Math.max(0, winEnd - s.windowSize + 1);
        if (winStart > winEnd) return;

        final int    r    = s.r, g = s.g, b = s.b;
        final float  w    = s.width;
        final int    al   = s.peakAlpha;
        final double camX = s.x, camY = s.y, camZ = s.z;

        final float COS30 = (float)(Math.sqrt(3.0) / 2.0);
        final float SIN30 = 0.5f;

        ps.pushPose();

        for (int i = winStart; i <= winEnd; i++) {
            Vec3 a  = trail.get(i);
            Vec3 b_ = trail.get(i + 1);
            Vec3 ra = new Vec3(a.x  - camX, a.y  - camY, a.z  - camZ);
            Vec3 rb = new Vec3(b_.x - camX, b_.y - camY, b_.z - camZ);

            Vec3 dir = rb.subtract(ra);
            if (dir.lengthSqr() < 1e-8) continue;
            dir = dir.normalize();

            Vec3 helper   = (Math.abs(dir.y) > 0.9) ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
            Vec3 right    = dir.cross(helper).normalize();
            Vec3 up       = dir.cross(right).normalize();

            Vec3 bisector = right.add(up).normalize();
            Vec3 perp     = right.subtract(up).normalize();
            Vec3 wingA    = bisector.scale(COS30).add(perp.scale(SIN30));
            Vec3 wingB    = bisector.scale(COS30).subtract(perp.scale(SIN30));

            final Vec3 pA = ra, pB = rb;
            final Vec3 pWingA = wingA, pWingB = wingB;
            final int  fr = r, fg = g, fb = b, fal = al;
            final float fw = w;

            snc.submitCustomGeometry(ps, RenderTypes.lightning(), (pose, v) -> {
                vQuad(pose, v, pA, pB, pWingA, fw, fr, fg, fb, fal);
                vQuad(pose, v, pA, pB, pWingB, fw, fr, fg, fb, fal);
            });
        }

        ps.popPose();
    }

    private static void vQuad(PoseStack.Pose pose, VertexConsumer v,
                              Vec3 a, Vec3 b, Vec3 axis, float w,
                              int r, int g, int bl, int al) {
        Vec3 off = axis.scale(w);
        Vec3 a0  = a,         a1 = a.add(off);
        Vec3 b0  = b,         b1 = b.add(off);
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