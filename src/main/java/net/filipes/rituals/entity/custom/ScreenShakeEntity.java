package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ScreenShakeEntity extends Entity {

    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(
            ScreenShakeEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Float> STRENGTH = SynchedEntityData.defineId(
            ScreenShakeEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Integer> DURATION = SynchedEntityData.defineId(
            ScreenShakeEntity.class,
            EntityDataSerializers.INT
    );

    public ScreenShakeEntity(EntityType<? extends ScreenShakeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
        this.setSilent(true);
    }

    public ScreenShakeEntity(Level level, Vec3 position, float radius, float strength, int duration) {
        this(ModEntities.SCREEN_SHAKE, level);
        this.setPos(position);
        this.setRadius(radius);
        this.setStrength(strength);
        this.setDuration(duration);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RADIUS, 12.0f);
        builder.define(STRENGTH, 1.0f);
        builder.define(DURATION, 80);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setRadius(input.getFloatOr("Radius", getRadius()));
        setStrength(input.getFloatOr("Strength", getStrength()));
        setDuration(input.getIntOr("Duration", getDuration()));
        this.tickCount = Math.max(0, input.getIntOr("Age", this.tickCount));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putFloat("Radius", getRadius());
        output.putFloat("Strength", getStrength());
        output.putInt("Duration", getDuration());
        output.putInt("Age", this.tickCount);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= getDuration()) {
            this.discard();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public float getRadius() {
        return this.entityData.get(RADIUS);
    }

    public void setRadius(float radius) {
        this.entityData.set(RADIUS, Math.max(0.5f, radius));
    }

    public float getStrength() {
        return this.entityData.get(STRENGTH);
    }

    public void setStrength(float strength) {
        this.entityData.set(STRENGTH, Math.max(0.0f, strength));
    }

    public int getDuration() {
        return this.entityData.get(DURATION);
    }

    public void setDuration(int duration) {
        this.entityData.set(DURATION, Math.max(1, duration));
    }
}