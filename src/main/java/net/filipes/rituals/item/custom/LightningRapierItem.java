package net.filipes.rituals.item.custom;

import net.filipes.rituals.effect.ModStatusEffects;
import net.filipes.rituals.sound.ModSounds;
import net.filipes.rituals.util.RitualsTooltipStyle;

// LivingEntity stays but moves to world.entity
import net.minecraft.world.entity.LivingEntity;

// StatusEffectInstance → MobEffectInstance, StatusEffect → MobEffect
import net.minecraft.world.effect.MobEffectInstance;

// Item, ItemStack stay in world.item
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

// ParticleTypes stays in core.particles
import net.minecraft.core.particles.ParticleTypes;

// ServerWorld → ServerLevel
import net.minecraft.server.level.ServerLevel;

// SoundCategory → SoundSource
import net.minecraft.sounds.SoundSource;

// World → Level
import net.minecraft.world.level.Level;

// Text → Component
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class LightningRapierItem extends Item implements RitualsTooltipStyle {

    private static final int STUN_DURATION_TICKS = 10;
    private static final double CHAIN_RADIUS = 8.0;
    private static final float CHAIN_DAMAGE = 3.0f;

    public LightningRapierItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

    // postHit() → hurtEnemy()
    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        applyStun(target);

        // getEntityWorld() → level()
        Level world = target.level();
        if (!world.isClientSide()) {
            ServerLevel serverWorld = (ServerLevel) world;

            // getEntitiesByClass() → getEntitiesOfClass()
            // getBoundingBox() → getBoundingBox() (unchanged)
            // expand() → inflate()
            List<LivingEntity> nearby = world.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(CHAIN_RADIUS),
                    entity -> entity != target && entity != attacker && entity.isAlive()
            );

            // squaredDistanceTo() → distanceToSqr()
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
                    // damage() → hurt(); getDamageSources() → damageSources()
                    chained.hurt(serverWorld.damageSources().lightningBolt(), CHAIN_DAMAGE);
                    previous = chained;
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
    }

    private void spawnLightningChain(ServerLevel world, LivingEntity from, LivingEntity to) {
        double x1 = from.getX(), y1 = from.getY(0.5), z1 = from.getZ();   // getBodyY() → getY(fraction)
        double x2 = to.getX(),   y2 = to.getY(0.5),   z2 = to.getZ();

        List<double[]> points = new ArrayList<>();
        points.add(new double[]{x1, y1, z1});
        points.add(new double[]{x2, y2, z2});

        int subdivisions = 7;
        for (int s = 0; s < subdivisions; s++) {
            List<double[]> next = new ArrayList<>();
            double displacement = 4.0 / (s + 1);
            for (int i = 0; i < points.size() - 1; i++) {
                double[] a = points.get(i);
                double[] b = points.get(i + 1);
                next.add(a);
                next.add(new double[]{
                        (a[0] + b[0]) / 2 + (world.getRandom().nextDouble() - 0.5) * displacement,
                        (a[1] + b[1]) / 2 + (world.getRandom().nextDouble() - 0.5) * displacement,
                        (a[2] + b[2]) / 2 + (world.getRandom().nextDouble() - 0.5) * displacement
                });
            }
            next.add(points.get(points.size() - 1));
            points = next;
        }

        for (int i = 0; i < points.size() - 1; i++) {
            double[] a = points.get(i);
            double[] b = points.get(i + 1);
            drawSegment(world, a[0], a[1], a[2], b[0], b[1], b[2]);
        }
    }

    private void drawSegment(ServerLevel world, double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int)(dist / 0.15));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;

            // spawnParticles() stays the same on ServerLevel
            world.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0, 0, 0, 0.0);
            world.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0, 0, 0, 0.0);
        }
    }

    private void applyStun(LivingEntity entity) {
        // addStatusEffect() → addEffect(); StatusEffectInstance → MobEffectInstance
        entity.addEffect(new MobEffectInstance(
                ModStatusEffects.STUN,
                STUN_DURATION_TICKS,
                0,
                false,
                true,
                true
        ));
    }

    // getName() → getDescription(); Text → Component; Text.translatable() → Component.translatable()
    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(s -> s.withColor(getNameColor()).withItalic(false));
    }

    @Override public int getNameColor()              { return 0xFF9B6DFF; }
    @Override public int getTooltipBorderColor()     { return 0xFFBB99FF; }
    @Override public int getTooltipBackgroundColor() { return 0xE5080020; }
}