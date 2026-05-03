package net.filipes.rituals.entity.custom;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;

public class SparkEntity extends ThrowableProjectile {
    public int maxLifetime = 80;
    public int trailLength = 20;
    public int trailGlowR = 255, trailGlowG = 160, trailGlowB = 30;
    public int trailCoreR = 255, trailCoreG = 240, trailCoreB = 200;
    public float trailGlowWidth = 0.10f;
    public float trailCoreWidth = 0.04f;
    public int trailAlpha = 200;

    public final ArrayDeque<Vec3> trailPositions = new ArrayDeque<>();


    private boolean launched = false;


    public SparkEntity(EntityType<? extends SparkEntity> type, Level level) {
        super(type, level);
    }

    public SparkEntity(EntityType<? extends SparkEntity> type, Level level,
                       double x, double y, double z) {
        this(type, level);
        setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {

    }

    @Override
    public void tick() {
        if (!level().isClientSide() && !launched) {
            launched = true;
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double speed = 0.25 + random.nextDouble() * 0.35;
            double vy    = 0.15 + random.nextDouble() * 0.25;
            setDeltaMovement(Math.cos(angle) * speed, vy, Math.sin(angle) * speed);
        }

        if (level().isClientSide()) {
            trailPositions.addFirst(new Vec3(getX(), getY(), getZ()));
            while (trailPositions.size() > trailLength) {
                trailPositions.removeLast();
            }
        }

        super.tick();

        if (!level().isClientSide() && tickCount >= maxLifetime) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (!level().isClientSide()) discard();
    }

    @Override
    protected double getDefaultGravity() { return 0.06; }

    @Override public boolean   shouldBeSaved()                                    { return false; }
    @Override protected void   readAdditionalSaveData(ValueInput in)              {}
    @Override protected void   addAdditionalSaveData(ValueOutput out)             {}
    @Override public boolean   hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean   canCollideWith(Entity e)                           { return false; }
    @Override public boolean   shouldRenderAtSqrDistance(double d)                { return d < 128 * 128; }
}