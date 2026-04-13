package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class ShadowguardHudOverlay {

    private static final long DURATION_MS = 3000;
    private static final long FADE_MS     = 500;

    private static long activeUntil = 0;

    public static void trigger() {
        activeUntil = System.currentTimeMillis() + DURATION_MS;
    }

    public static boolean isActive() {
        return System.currentTimeMillis() < activeUntil;
    }

    public static void register() {
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("rituals", "shadowguard_hud"),
                new HudElement() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
                        render(graphics);
                    }
                }
        );
    }

    private static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        long remaining = activeUntil - System.currentTimeMillis();
        if (remaining <= 0) return;

        float alpha;
        if (remaining < FADE_MS) {
            alpha = (float) remaining / FADE_MS;
        } else if (DURATION_MS - remaining < FADE_MS) {
            alpha = (float)(DURATION_MS - remaining) / FADE_MS;
        } else {
            alpha = 1.0f;
        }

        int screenWidth  = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Vignette: thin but intense gradients from each edge, mimicking world border aura
        int depth  = screenHeight / 4;
        int maxA   = (int)(alpha * 180) & 0xFF;
        int opaque = (maxA << 24) | 0xAA0000;
        int transp = 0x00AA0000;

        guiGraphics.fillGradient(0, 0,            screenWidth, depth,                    opaque, transp); // top
        guiGraphics.fillGradient(0, screenHeight - depth, screenWidth, screenHeight,     transp, opaque); // bottom
        guiGraphics.fillGradient(0, 0,            depth, screenHeight,                   opaque, transp); // left
        guiGraphics.fillGradient(screenWidth - depth, 0, screenWidth, screenHeight,      transp, opaque); // right

        // [INVISIBLE] just above the hotbar
        String label    = "[INVISIBLE]";
        int textWidth   = mc.font.width(label);
        int textAlpha   = (int)(alpha * 255) & 0xFF;

        guiGraphics.text(
                mc.font,
                label,
                (screenWidth - textWidth) / 2,
                screenHeight - 45,
                (textAlpha << 24) | 0x9B6DFF,
                true
        );
    }
}