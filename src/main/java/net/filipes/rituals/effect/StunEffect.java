package net.filipes.rituals.effect;

import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StunEffect extends MobEffect {

    public StunEffect() {
        // Constructor now takes MobEffectCategory and the color int
        super(MobEffectCategory.HARMFUL, 0x9B6DFF);

        // addAttributeModifier -> addAttributeModifier
        // EntityAttributes -> Attributes
        // Identifier -> ResourceLocation
        // EntityAttributeModifier.Operation -> AttributeModifier.Operation
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                Identifier.fromNamespaceAndPath("rituals", "stun_slowness"),
                -0.9,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }
}