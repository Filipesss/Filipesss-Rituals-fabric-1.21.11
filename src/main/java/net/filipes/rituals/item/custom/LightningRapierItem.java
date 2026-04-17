package net.filipes.rituals.item.custom;

import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.entity.custom.ElectricBoltEntity;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import java.util.List;

import static java.lang.Math.sqrt;

public class LightningRapierItem extends Item implements RitualsTooltipStyle {

    private static final int STUN_DURATION_TICKS = 10;
    private static final double CHAIN_RADIUS = 8.0;
    private static final float CHAIN_DAMAGE = 3.0f;

    public LightningRapierItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyStun(target);

        Level world = target.level();
        if (!world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;

            List<LivingEntity> nearby = world.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(CHAIN_RADIUS),
                    entity -> entity != target && entity != attacker && entity.isAlive()
            );

            nearby.sort((a, b) -> Double.compare(
                    a.distanceToSqr(target),
                    b.distanceToSqr(target)
            ));

            if (nearby.isEmpty()) {
                world.playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        ModSounds.LIGHTNING_RAPIER_ATTACK2,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            } else {
                world.playSound(null,
                        target.getX(), target.getY(), target.getZ(),
                        ModSounds.LIGHTNING_RAPIER_ATTACK1,
                        SoundSource.PLAYERS, 1.0f, 1.0f);

                LivingEntity previous = target;
                for (LivingEntity chained : nearby) {
                    spawnLightningChain(serverWorld, previous, chained);
                    applyStun(chained);
                    chained.hurt(serverWorld.damageSources().lightningBolt(), CHAIN_DAMAGE);
                    previous = chained;
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }

    private void spawnLightningChain(ServerLevel world, LivingEntity from, LivingEntity to) {
        ElectricBoltEntity.spawn(
                world,
                from.getEyePosition(),
                to.getEyePosition(),
                0.6f,
                0.14f,
                0x98E8FF
        );

        drawSegment(world,
                from.getX(), from.getY(0.5), from.getZ(),
                to.getX(), to.getY(0.5), to.getZ()
        );
    }

    private void drawSegment(ServerLevel world, double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double dist = sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int)(dist / 0.15));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;

            world.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0.0);
            world.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0.0);
        }
    }

    private void applyStun(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                ModStatusEffects.STUN,
                STUN_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(s -> s.withColor(getNameColor()).withItalic(false));
    }


    @Override
    public int getNameColor() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0xFF550000;
    }
}