package net.filipes.rituals.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.network.PulseBlasterAmmoPayload;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class PulseBlasterHudOverlay {

    private static final int SEGMENTS = 8;
    private static final int SEG_W    = 7;
    private static final int SEG_H    = 10;
    private static final int SEG_GAP  = 2;
    private static final int BAR_W    = SEGMENTS * SEG_W + (SEGMENTS - 1) * SEG_GAP;

    private static int liveAmmo = -1;

    public static void setLiveAmmo(int ammo) { liveAmmo = ammo; }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                PulseBlasterAmmoPayload.ID,
                (payload, context) -> {
                    int newAmmo = payload.ammo();
                    if (newAmmo >= 0 && liveAmmo > newAmmo) {
                        PulseBlasterCylinderState.onShot();
                    }
                    liveAmmo = newAmmo;
                }
        );

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_hud"),
                new HudElement() {
                    @Override
                    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
                        renderHud(graphics);
                    }
                }
        );
    }

    private static void renderHud(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        ItemStack main = client.player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off  = client.player.getItemInHand(InteractionHand.OFF_HAND);

        ItemStack blaster = null;
        if (main.getItem() instanceof PulseBlasterItem) blaster = main;
        else if (off.getItem() instanceof PulseBlasterItem) blaster = off;
        if (blaster == null) { liveAmmo = -1; return; }

        int ammo    = (liveAmmo >= 0) ? liveAmmo : PulseBlasterItem.getAmmo(blaster);
        int maxAmmo = PulseBlasterItem.MAX_AMMO;

        int screenW    = client.getWindow().getGuiScaledWidth();
        int screenH    = client.getWindow().getGuiScaledHeight();
        int hotbarLeft = (screenW - 182) / 2;
        int x          = hotbarLeft - BAR_W - 14;
        int y          = screenH - 22;

        boolean low     = ammo <= 2 && ammo > 0;
        boolean empty   = ammo == 0;
        long    time    = System.currentTimeMillis();
        boolean flashOn = !low || (time / 350) % 2 == 0;

        for (int i = 0; i < maxAmmo; i++) {
            int     sx     = x + i * (SEG_W + SEG_GAP);
            boolean filled = i < ammo;

            if (filled) {
                float ratio = (float) ammo / maxAmmo;
                int   fill  = flashOn ? blendColor(ratio) : 0xFF888888;


                graphics.fill(sx - 1, y - 1, sx + SEG_W + 1, y + SEG_H + 1, darken(fill, 0.4f));
                graphics.fill(sx,     y,     sx + SEG_W,     y + SEG_H,     fill);
                graphics.fill(sx,     y,     sx + SEG_W,     y + 2,         brighten(fill, 1.6f));
                graphics.fill(sx,     y + SEG_H - 2, sx + SEG_W, y + SEG_H, darken(fill, 0.6f));
            } else {
                graphics.fill(sx - 1, y - 1, sx + SEG_W + 1, y + SEG_H + 1, 0xFF1A1A1A);
                graphics.fill(sx,     y,     sx + SEG_W,     y + SEG_H,     0xFF2A2A2A);
                graphics.fill(sx,     y,     sx + SEG_W,     y + 1,         0xFF111111);
            }
        }

        String label;
        int    labelColor;
        if (empty) {
            label = (time / 500) % 2 == 0 ? "RELOAD" : ""; labelColor = 0xFFAA3333;
        } else if (low) {
            label = "LOW  " + ammo + "/" + maxAmmo; labelColor = flashOn ? 0xFFFF4444 : 0xFF883333;
        } else {
            label = "PWR  " + ammo + "/" + maxAmmo; labelColor = 0xFFAAAAAA;
        }

        int labelX = x + BAR_W / 2 - client.font.width(label) / 2;
        graphics.text(client.font, label, labelX, y - 10, labelColor, true);
    }

    private static int blendColor(float ratio) {
        int r, g, b;
        if (ratio >= 0.5f) {
            float t = (ratio - 0.5f) * 2f;
            r = lerp(0xFF, 0x00, t); g = lerp(0xAA, 0xBB, t); b = lerp(0x00, 0xFF, t);
        } else {
            float t = ratio * 2f;
            r = lerp(0xFF, 0xFF, t); g = lerp(0x22, 0xAA, t); b = lerp(0x00, 0x00, t);
        }
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int darken(int color, float f) {
        int r = (int)(((color >> 16) & 0xFF) * f);
        int g = (int)(((color >>  8) & 0xFF) * f);
        int b = (int)(( color        & 0xFF) * f);
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int brighten(int color, float f) {
        int r = (int)(((color >> 16) & 0xFF) * f);
        int g = (int)(((color >>  8) & 0xFF) * f);
        int b = (int)(( color        & 0xFF) * f);
        return 0xFF000000 | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int lerp(int a, int b, float t) { return (int)(a + (b - a) * t); }
    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}