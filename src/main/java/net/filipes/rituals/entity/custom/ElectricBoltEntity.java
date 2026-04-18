package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
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
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class ElectricBoltEntity extends Entity {

    private static final EntityDataAccessor<Vector3fc> ORIGIN = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.VECTOR3
    );

    private static final EntityDataAccessor<Vector3fc> DIRECTION = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.VECTOR3
    );

    private static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Float> DISTANCE = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Float> WIDTH = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.FLOAT
    );

    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.INT
    );

    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(
            ElectricBoltEntity.class,
            EntityDataSerializers.INT
    );

    public ElectricBoltEntity(EntityType<? extends ElectricBoltEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvisible(true);
        this.setSilent(true);
    }

    public ElectricBoltEntity(Level level, Vec3 start, Vec3 end) {
        this(level, start, end, 4.0f, 0.12f, 0x98E8FF);
    }

    public ElectricBoltEntity(Level level, Vec3 start, Vec3 end, float speed, float width, int color) {
        this(ModEntities.ELECTRIC_BOLT, level);
        configureLine(start, end, speed, width, color);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ORIGIN, new Vector3f());
        builder.define(DIRECTION, new Vector3f(0.0f, 0.0f, 1.0f));
        builder.define(SPEED, 4.0f);
        builder.define(DISTANCE, 0.0f);
        builder.define(WIDTH, 0.12f);
        builder.define(COLOR, 0x98E8FF);
        builder.define(LIFETIME, 400);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setOrigin(readVec3(input, "Origin", this.position()));
        setBoltDirection(readVec3(input, "Direction", new Vec3(0.0, 0.0, 1.0)));
        setSpeed(input.getFloatOr("Speed", getSpeed()));
        setDistance(input.getFloatOr("Distance", getDistance()));
        setWidth(input.getFloatOr("Width", getWidth()));
        setColor(input.getIntOr("Color", getColor()));
        setLifetime(input.getIntOr("Lifetime", getLifetime()));
        this.tickCount = Math.max(0, input.getIntOr("Age", this.tickCount));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        writeVec3(output, "Origin", getOrigin());
        writeVec3(output, "Direction", getBoltDirection());
        output.putFloat("Speed", getSpeed());
        output.putFloat("Distance", getDistance());
        output.putFloat("Width", getWidth());
        output.putInt("Color", getColor());
        output.putInt("Lifetime", getLifetime());
        output.putInt("Age", this.tickCount);
    }

    @Override
    public void tick() {
        super.tick();

        Vec3 origin = getOrigin();
        Vec3 direction = getBoltDirection();
        double progress = Math.min(getDistance(), Math.max(0.0, (this.tickCount - 1) * getSpeed()));
        Vec3 current = origin.add(direction.scale(progress));
        this.setPos(current.x, current.y, current.z);

        if (!this.level().isClientSide() && this.tickCount % 2 == 0) {
            Vec3 trail = origin.add(direction.scale(Math.max(0.0, progress - getSpeed() * 0.55)));
            ServerLevel serverLevel = (ServerLevel) this.level();

            serverLevel.addParticle(ParticleTypes.ELECTRIC_SPARK, current.x, current.y, current.z, 0.0, 0.0, 0.0);
            serverLevel.addParticle(ParticleTypes.END_ROD, trail.x, trail.y, trail.z, 0.0, 0.0, 0.0);
        }

        if (this.tickCount >= getLifetime() || progress >= getDistance()) {
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
        return true;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public Vec3 getOrigin() {
        return toVec3(this.entityData.get(ORIGIN));
    }

    public void setOrigin(Vec3 origin) {
        this.entityData.set(ORIGIN, new Vector3f((float) origin.x, (float) origin.y, (float) origin.z));
    }

    public Vec3 getBoltDirection() {
        return toVec3(this.entityData.get(DIRECTION));
    }

    public void setBoltDirection(Vec3 direction) {
        Vec3 normalized = normalizeOrDefault(direction);
        this.entityData.set(DIRECTION, new Vector3f((float) normalized.x, (float) normalized.y, (float) normalized.z));
    }

    public float getSpeed() {
        return this.entityData.get(SPEED);
    }

    public void setSpeed(float speed) {
        this.entityData.set(SPEED, Math.max(0.05f, speed));
    }

    public float getDistance() {
        return this.entityData.get(DISTANCE);
    }

    public void setDistance(float distance) {
        this.entityData.set(DISTANCE, Math.max(0.0f, distance));
    }

    public float getWidth() {
        return this.entityData.get(WIDTH);
    }

    public void setWidth(float width) {
        this.entityData.set(WIDTH, Math.max(0.01f, width));
    }

    public int getColor() {
        return this.entityData.get(COLOR);
    }

    public void setColor(int color) {
        this.entityData.set(COLOR, color | 0xFF000000);
    }

    public int getLifetime() {
        return this.entityData.get(LIFETIME);
    }

    public void setLifetime(int lifetime) {
        this.entityData.set(LIFETIME, Math.max(1, lifetime));
    }

    public void configureLine(Vec3 start, Vec3 end, float speed, float width, int color) {
        Vec3 delta = end.subtract(start);
        double distance = delta.length();
        Vec3 direction = distance < 0.0001 ? new Vec3(0.0, 0.0, 1.0) : delta.scale(1.0 / distance);

        setOrigin(start);
        setBoltDirection(direction);
        setSpeed(speed);
        setDistance((float) Math.max(0.0, distance));
        setWidth(width);
        setColor(color);
        setLifetime(Math.max(2, (int) Math.ceil(distance / getSpeed()) + 2));
        this.setPos(start.x, start.y, start.z);
        this.setDeltaMovement(direction.scale(speed));
    }

    public static ElectricBoltEntity spawn(ServerLevel level, Vec3 start, Vec3 end, float speed, float width, int color) {
        ElectricBoltEntity entity = new ElectricBoltEntity(level, start, end, speed, width, color);
        level.addFreshEntity(entity);
        return entity;
    }

    private static Vec3 readVec3(ValueInput input, String prefix, Vec3 fallback) {
        double x = input.getDoubleOr(prefix + "X", fallback.x);
        double y = input.getDoubleOr(prefix + "Y", fallback.y);
        double z = input.getDoubleOr(prefix + "Z", fallback.z);
        return new Vec3(x, y, z);
    }

    private static void writeVec3(ValueOutput output, String prefix, Vec3 value) {
        output.putDouble(prefix + "X", value.x);
        output.putDouble(prefix + "Y", value.y);
        output.putDouble(prefix + "Z", value.z);
    }

    private static Vec3 toVec3(Vector3fc value) {
        return new Vec3(value.x(), value.y(), value.z());
    }

    private static Vec3 normalizeOrDefault(Vec3 direction) {
        double length = direction.length();
        if (length < 0.0001) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return direction.scale(1.0 / length);
    }


}