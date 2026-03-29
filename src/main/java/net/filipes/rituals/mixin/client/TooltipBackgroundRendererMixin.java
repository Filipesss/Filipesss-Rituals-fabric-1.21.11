package net.filipes.rituals.mixin.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.filipes.rituals.util.TooltipStyleHolder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TooltipRenderUtil.class)
public class TooltipBackgroundRendererMixin {

    @Redirect(
            method = "extractTooltipBackground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;" +
                            "blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;" +
                            "Lnet/minecraft/resources/Identifier;IIII)V"
            )
    )
    private static void redirectTooltipTextures(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            Identifier texture,
            int x, int y, int width, int height) {

        RitualsTooltipStyle style = TooltipStyleHolder.currentStyle;
        if (style != null) {
            if (texture.getPath().contains("background")) {
                // Fill the inner area, trimming the 9px sprite padding
                graphics.fill(x + 9, y + 9, x + width - 9, y + height - 9,
                        style.getTooltipBackgroundColor());
            } else {
                // Draw border as a stroked rectangle outline
                graphics.outline(x + 9, y + 9, width - 18, height - 18,
                        style.getTooltipBorderColor());
            }
        } else {
            // Vanilla fallback
            graphics.blitSprite(pipeline, texture, x, y, width, height);
        }
    }
}