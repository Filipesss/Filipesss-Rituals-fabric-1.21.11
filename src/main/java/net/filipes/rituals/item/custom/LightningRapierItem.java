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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

import java.util.List;

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
                    ElectricBoltEntity.spawn(
                            serverWorld,
                            previous.getEyePosition(),
                            chained.getEyePosition(),
                            2.0f,
                            0.14f,
                            0x98E8FF
                    );

                    applyStun(chained);
                    chained.hurt(serverWorld.damageSources().lightningBolt(), CHAIN_DAMAGE);
                    previous = chained;
                }
            }
        }

        super.hurtEnemy(stack, target, attacker);
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