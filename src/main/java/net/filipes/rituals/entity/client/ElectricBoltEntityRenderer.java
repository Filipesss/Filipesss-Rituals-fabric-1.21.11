package net.filipes.rituals.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.filipes.rituals.entity.custom.ElectricBoltEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ElectricBoltEntityRenderer extends EntityRenderer<ElectricBoltEntity, ElectricBoltEntityRenderer.ElectricBoltRenderState> {

    public static class ElectricBoltRenderState extends EntityRenderState {
        public Vec3 originLocal = new Vec3(0.0, 0.0, 0.0);
        public Vec3 direction = new Vec3(0.0, 1.0, 0.0);
        public float maxDistance = 0.0f;
        public float currentProgress = 0.0f;
        public float width = 0.12f;
        public int color = 0x98E8FF;
        public long seed = 0L;
    }

    public static class Branch {
        public final List<Vec3> points;
        public final float widthFactor;

        public Branch(List<Vec3> points, float widthFactor) {
            this.points = points;
            this.widthFactor = widthFactor;
        }
    }

    public ElectricBoltEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ElectricBoltRenderState createRenderState() {
        return new ElectricBoltRenderState();
    }

    @Override
    public void extractRenderState(ElectricBoltEntity entity, ElectricBoltRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        Vec3 current = new Vec3(state.x, state.y, state.z);

        state.originLocal = entity.getOrigin().subtract(current);
        state.direction = entity.getBoltDirection();
        state.maxDistance = entity.getDistance();

        float age = entity.tickCount - 1 + partialTicks;
        state.currentProgress = Math.min(state.maxDistance, Math.max(0.0f, age * entity.getSpeed()));

        state.width = entity.getWidth();
        state.color = entity.getColor();

        state.seed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
    }

    @Override
    public boolean affectedByCulling(ElectricBoltEntity entity) {
        return false;
    }

    @Override
    public void submit(ElectricBoltRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.maxDistance < 0.0001f || state.currentProgress <= 0.0001f) {
            return;
        }

        long seed = state.seed;
        RandomSource random = RandomSource.createThreadLocalInstance(seed);
        List<Branch> branches = new ArrayList<>();

        double step = 0.65;

        buildBranch(branches, state.originLocal, state.direction, state.maxDistance, step, random, 0, 1.0f, 0.0, state.currentProgress);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.lightning(),
                (pose, vertices) -> {
                    for (Branch branch : branches) {
                        drawBoltBranch(pose, vertices, branch, state.width, state.color);
                    }
                }
        );
    }

    @Override
    protected float getShadowRadius(ElectricBoltRenderState state) {
        return 0.0f;
    }

    @Override
    protected float getShadowStrength(ElectricBoltRenderState state) {
        return 0.0f;
    }

    private static void buildBranch(List<Branch> branches, Vec3 startPos, Vec3 dir, double length, double step,
                                    RandomSource random, int depth, float widthFactor,
                                    double currentPathDist, double maxAllowedDist) {
        if (depth > 3 || length <= 0.1 || widthFactor < 0.2f) return;

        List<Vec3> points = new ArrayList<>();
        points.add(startPos);

        int segments = (int) Math.ceil(length / step);

        for (int i = 1; i <= segments; i++) {
            currentPathDist += step;

            if (currentPathDist > maxAllowedDist) {
                double over = currentPathDist - maxAllowedDist;
                double ratio = 1.0 - (over / step);
                if (ratio > 0.05) {
                    Vec3 idealPos = startPos.add(dir.scale((i - 1 + ratio) * (length / segments)));
                    points.add(idealPos);
                }
                break;
            }

            double t = (double) i / segments;
            Vec3 idealPos = startPos.add(dir.scale(t * length));

            double jitter = 0.9 * widthFactor;
            Vec3 noise = new Vec3(
                    (random.nextDouble() - 0.5) * jitter,
                    (random.nextDouble() - 0.5) * jitter,
                    (random.nextDouble() - 0.5) * jitter
            );

            Vec3 nextPos = idealPos.add(noise);
            points.add(nextPos);

            if (random.nextFloat() < 0.25f && depth < 3) {
                Vec3 branchDir = dir.add(new Vec3(
                        (random.nextDouble() - 0.5) * 1.5,
                        (random.nextDouble() - 0.5) * 1.5,
                        (random.nextDouble() - 0.5) * 1.5
                )).normalize();

                double branchLength = length * (0.3 + random.nextDouble() * 0.4);
                buildBranch(branches, nextPos, branchDir, branchLength, step, random, depth + 1, widthFactor * 0.6f, currentPathDist, maxAllowedDist);
            }
        }

        if (points.size() > 1) {
            branches.add(new Branch(points, widthFactor));
        }
    }

    private static void drawBoltBranch(PoseStack.Pose pose, VertexConsumer vertices, Branch branch, float baseWidth, int baseColor) {
        float width = baseWidth * branch.widthFactor;
        int coreColor = tint(0xFFFFFF, 1.0f, 1.0f);
        int outerColor = fadeColor(baseColor, 0.65f);

        for (int i = 0; i < branch.points.size() - 1; i++) {
            Vec3 a = branch.points.get(i);
            Vec3 b = branch.points.get(i + 1);

            draw3DSegment(pose, vertices, a, b, width * 1.8f, outerColor);
            draw3DSegment(pose, vertices, a, b, width * 0.5f, coreColor);
        }
    }

    private static void draw3DSegment(PoseStack.Pose pose, VertexConsumer vertices, Vec3 a, Vec3 b, float width, int color) {
        Vec3 forward = b.subtract(a);
        if (forward.lengthSqr() < 0.0001) return;
        forward = forward.normalize();

        Vec3 upRef = (Math.abs(forward.y) > 0.85) ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = forward.cross(upRef).normalize();
        Vec3 up = right.cross(forward).normalize();

        float w = width * 0.5f;

        Vec3 a1 = a.add(right.scale(w)).add(up.scale(w));
        Vec3 a2 = a.subtract(right.scale(w)).add(up.scale(w));
        Vec3 a3 = a.subtract(right.scale(w)).subtract(up.scale(w));
        Vec3 a4 = a.add(right.scale(w)).subtract(up.scale(w));

        Vec3 b1 = b.add(right.scale(w)).add(up.scale(w));
        Vec3 b2 = b.subtract(right.scale(w)).add(up.scale(w));
        Vec3 b3 = b.subtract(right.scale(w)).subtract(up.scale(w));
        Vec3 b4 = b.add(right.scale(w)).subtract(up.scale(w));

        drawQuad(pose, vertices, a1, a2, b2, b1, color);
        drawQuad(pose, vertices, a4, a3, b3, b4, color);
        drawQuad(pose, vertices, a2, a3, b3, b2, color);
        drawQuad(pose, vertices, a1, a4, b4, b1, color);

        drawQuad(pose, vertices, a2, a1, a4, a3, color);
        drawQuad(pose, vertices, b1, b2, b3, b4, color);
    }

    private static void drawQuad(PoseStack.Pose pose, VertexConsumer vertices, Vec3 a, Vec3 b, Vec3 c, Vec3 d, int color) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;

        vertices.addVertex(pose, (float) a.x, (float) a.y, (float) a.z).setColor(red, green, blue, alpha);
        vertices.addVertex(pose, (float) b.x, (float) b.y, (float) b.z).setColor(red, green, blue, alpha);
        vertices.addVertex(pose, (float) c.x, (float) c.y, (float) c.z).setColor(red, green, blue, alpha);
        vertices.addVertex(pose, (float) d.x, (float) d.y, (float) d.z).setColor(red, green, blue, alpha);
    }

    private static int tint(int color, float saturationBoost, float alphaScale) {
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (color >>> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        alpha = (int) (alpha * alphaScale);

        red = clamp((int) (red + (255 - red) * saturationBoost));
        green = clamp((int) (green + (255 - green) * saturationBoost));
        blue = clamp((int) (blue + (255 - blue) * saturationBoost));

        return (clamp(alpha) << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int fadeColor(int color, float scale) {
        int alpha = (color >>> 24) & 0xFF;
        if (alpha == 0) alpha = 255;
        alpha = clamp((int) (alpha * scale));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}