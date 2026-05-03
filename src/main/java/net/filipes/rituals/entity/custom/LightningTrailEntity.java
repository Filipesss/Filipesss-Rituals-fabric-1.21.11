package net.filipes.rituals.entity.custom;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LightningTrailEntity extends Entity {

    public static final int   FRAME_COUNT = 8;
    public static final float QUAD_SIZE   = 2.0f;   // matches particle quadSize

    public LightningTrailEntity(EntityType<? extends LightningTrailEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public LightningTrailEntity(EntityType<? extends LightningTrailEntity> type, Level level,
                                double x, double y, double z) {
        this(type, level);
        this.setPos(x, y, z);
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        xo = getX();
        yo = getY();
        zo = getZ();

        // After all frames have played, remove the entity
        if (tickCount >= FRAME_COUNT) {
            discard();
        }
    }

    /** Current animation frame (0-based, clamped for safety). */
    public int getCurrentFrame() {
        return Math.min(tickCount, FRAME_COUNT - 1);
    }

    // ── Boilerplate ───────────────────────────────────────────────────────────

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override public boolean      shouldBeSaved()                              { return false; }
    @Override protected void      readAdditionalSaveData(ValueInput in)        {}
    @Override protected void      addAdditionalSaveData(ValueOutput out)       {}
    @Override public PushReaction  getPistonPushReaction()                     { return PushReaction.IGNORE; }
    @Override public boolean       isPickable()                                { return false; }
    @Override public boolean       isPushable()                                { return false; }
    @Override public boolean       shouldRenderAtSqrDistance(double d)        { return d < (256.0 * 256.0); }
    @Override public boolean       hurtServer(ServerLevel l, DamageSource s, float a) { return false; }
    @Override public boolean       canCollideWith(Entity e)                    { return false; }
    @Override public boolean       canBeCollidedWith(Entity e)                 { return false; }
}