package net.filipes.rituals.mixin;

import net.filipes.rituals.item.custom.RosegoldPickaxeItem;
import net.filipes.rituals.item.custom.ShadowguardItem;
import net.filipes.rituals.component.ModDataComponents;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "isEnchantable", at = @At("HEAD"), cancellable = true)
    private void rituals$gateEnchanting(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack)(Object)this;

        if (self.getItem() instanceof RosegoldPickaxeItem) {
            if (RosegoldPickaxeItem.getStage(self) < 2) {
                cir.setReturnValue(false);
            }
        }

        // Shadowguard: no enchants until stage 3 (Wind Burst)
        if (self.getItem() instanceof ShadowguardItem) {
            if (ModDataComponents.getStage(self) < 3) {
                cir.setReturnValue(false);
            }
        }
    }
}