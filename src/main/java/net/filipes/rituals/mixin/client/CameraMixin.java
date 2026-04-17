package net.filipes.rituals.mixin.client;

import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    public abstract Vec3 position();

    @Shadow
    public abstract float xRot();

    @Shadow
    public abstract float yRot();

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "update", at = @At("TAIL"))
    private void rituals$applyScreenShake(DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        double totalYaw = 0.0;
        double totalPitch = 0.0;
        double totalX = 0.0;
        double totalY = 0.0;
        double totalZ = 0.0;
        float time = level.getGameTime() + deltaTracker.getGameTimeDeltaPartialTick(false);

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ScreenShakeEntity shake)) {
                continue;
            }

            double radius = shake.getRadius();
            double distanceSqr = minecraft.player.distanceToSqr(entity);
            double radiusSqr = radius * radius;
            if (distanceSqr > radiusSqr) {
                continue;
            }

            double distance = Math.sqrt(distanceSqr);
            float distanceFalloff = 1.0f - (float) (distance / radius);
            float lifeFalloff = 1.0f - ((float) shake.tickCount / (float) Math.max(1, shake.getDuration()));
            float strength = shake.getStrength() * distanceFalloff * Math.max(0.0f, lifeFalloff);

            if (strength <= 0.0f) {
                continue;
            }

            long seed = shake.getUUID().getMostSignificantBits() ^ shake.getUUID().getLeastSignificantBits();
            double phase = time * 0.6 + (seed & 1023L) * 0.01;

            totalYaw += Math.sin(phase * 1.9 + (seed & 31L) * 0.1) * strength * 4.0;
            totalPitch += Math.cos(phase * 2.3 + ((seed >> 5) & 31L) * 0.1) * strength * 3.0;
            totalX += Math.sin(phase * 2.9) * strength * 0.03;
            totalY += Math.cos(phase * 1.7) * strength * 0.02;
            totalZ += Math.sin(phase * 2.1 + 1.0) * strength * 0.03;
        }

        if (totalYaw == 0.0 && totalPitch == 0.0 && totalX == 0.0 && totalY == 0.0 && totalZ == 0.0) {
            return;
        }

        float yawOffset = (float) Math.max(-8.0, Math.min(8.0, totalYaw));
        float pitchOffset = (float) Math.max(-8.0, Math.min(8.0, totalPitch));
        double xOffset = Math.max(-0.15, Math.min(0.15, totalX));
        double yOffset = Math.max(-0.10, Math.min(0.10, totalY));
        double zOffset = Math.max(-0.15, Math.min(0.15, totalZ));

        setRotation(yRot() + yawOffset, xRot() + pitchOffset);
        setPosition(position().add(xOffset, yOffset, zOffset));
    }
}