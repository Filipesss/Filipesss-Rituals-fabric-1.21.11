package net.filipes.rituals.mixin.client;

import net.filipes.rituals.util.ShadowguardStateAccess;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ShadowguardStateAccess {

    @Unique private boolean rituals$shadowguardInvisible = false;

    @Override
    public boolean rituals$isShadowguardInvisible() {
        return rituals$shadowguardInvisible;
    }

    @Override
    public void rituals$setShadowguardInvisible(boolean v) {
        rituals$shadowguardInvisible = v;
    }
}