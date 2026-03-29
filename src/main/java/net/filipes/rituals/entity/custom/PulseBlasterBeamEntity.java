package net.filipes.rituals.entity.custom;

import net.filipes.rituals.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PulseBlasterBeamEntity extends Projectile {

    private static final float DAMAGE     = 8.0f;
    private static final int   FIRE_SECONDS = 5; // renamed from FIRE_TICKS since the method takes seconds
    private static final float BEAM_SPEED = 1.5f;
    private static final int   MAX_AGE    = 80;

    private final Level storedWorld; // 'World' is now 'Level'

    public PulseBlasterBeamEntity(
            EntityType<? extends PulseBlasterBeamEntity> type,
            Level level
    ) {
        super(type, level);
        this.storedWorld = level;
        this.setNoGravity(true);
    }

    public PulseBlasterBeamEntity(Level level, LivingEntity owner) {
        this(ModEntities.PULSE_BLASTER_BEAM, level);

        this.setOwner(owner);

        // setPosition -> setPos
        this.setPos(
                owner.getX(),
                owner.getEyeY() - 0.1,
                owner.getZ()
        );

        // setVelocity -> shootFromRotation
        this.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0f, BEAM_SPEED, 0.0f);
    }

    // initDataTracker -> defineSynchedData
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) { }

    @Override
    public void tick() {
        super.tick();

        // Vec3d -> Vec3, getEntityPos -> position, getVelocity -> getDeltaMovement
        Vec3 currentPos = this.position();
        Vec3 velocity = this.getDeltaMovement();
        Vec3 nextPos = currentPos.add(velocity);

        // getEntityCollision -> getEntityHitResult
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                storedWorld,
                this,
                currentPos,
                nextPos,
                // stretch -> expandTowards, expand -> inflate
                this.getBoundingBox().expandTowards(velocity).inflate(1.0),
                entity -> !entity.isSpectator() && entity != this.getOwner()
        );

        // isClient -> isClientSide
        if (entityHit != null && !storedWorld.isClientSide()) {
            Entity target = entityHit.getEntity();
            ServerLevel serverLevel = (ServerLevel) storedWorld;

            // In recent versions, damage is handled via hurtServer
            target.hurtServer(
                    serverLevel,
                    // getDamageSources() -> damageSources()
                    serverLevel.damageSources().thrown(this, this.getOwner()),
                    DAMAGE
            );
            // setOnFireFor -> igniteForSeconds
            target.igniteForSeconds(FIRE_SECONDS);
            this.discard();
            return;
        }

        // raycast -> clip, RaycastContext -> ClipContext
        BlockHitResult blockHit = storedWorld.clip(new ClipContext(
                currentPos,
                nextPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        if (blockHit.getType() != HitResult.Type.MISS && !storedWorld.isClientSide()) {
            // offset(getSide) -> relative(getDirection)
            BlockPos firePos = blockHit.getBlockPos().relative(blockHit.getDirection());

            // isAir -> isEmptyBlock
            if (storedWorld.isEmptyBlock(firePos)) {
                // setBlockState -> setBlockAndUpdate
                // AbstractFireBlock -> BaseFireBlock
                storedWorld.setBlockAndUpdate(
                        firePos,
                        BaseFireBlock.getState(storedWorld, firePos)
                );
            }
            this.discard();
            return;
        }

        // Move the entity
        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        // age -> tickCount
        if (this.tickCount > MAX_AGE) {
            this.discard();
        }
    }
}