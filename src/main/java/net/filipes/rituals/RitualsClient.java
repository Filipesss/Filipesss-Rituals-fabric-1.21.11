package net.filipes.rituals;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.filipes.rituals.blocks.entity.ModBlockEntities;
import net.filipes.rituals.client.PulseBlasterCylinderState;
import net.filipes.rituals.client.PulseBlasterGunModel;
import net.filipes.rituals.client.PulseBlasterHudOverlay;
import net.filipes.rituals.client.PulseBlasterSpecialRenderer;
import net.filipes.rituals.client.render.RitualPedestalBlockEntityRenderer;
import net.filipes.rituals.entity.ModEntities;
import net.filipes.rituals.entity.client.PulseBlasterBeamModel;
import net.filipes.rituals.entity.client.PulseBlasterBeamRenderer;
import net.filipes.rituals.util.TooltipStyleHolder;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;

public class RitualsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        // ── Tooltip style API ──────────────────────────────────────────────────
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            TooltipStyleHolder.clear();
            TooltipStyleHolder.set(stack);
        });

        // ── Block entity renderers ─────────────────────────────────────────────
        BlockEntityRenderers.register(
                ModBlockEntities.RITUAL_PEDESTAL_BE,
                RitualPedestalBlockEntityRenderer::new
        );

        // ── Beam projectile entity ─────────────────────────────────────────────
        EntityRendererRegistry.register(
                ModEntities.PULSE_BLASTER_BEAM,
                PulseBlasterBeamRenderer::new
        );
        ModelLayerRegistry.registerModelLayer(
                PulseBlasterBeamModel.LAYER,
                PulseBlasterBeamModel::createBodyLayer
        );

        // ── Pulse Blaster gun item — special renderer with rotating cylinder ───
        ModelLayerRegistry.registerModelLayer(
                PulseBlasterGunModel.LAYER,
                PulseBlasterGunModel::getTexturedModelData
        );

        SpecialModelRenderers.ID_MAPPER.put(
                Identifier.fromNamespaceAndPath("rituals", "pulse_blaster"),
                (MapCodec<? extends SpecialModelRenderer.Unbaked<?>>) (MapCodec<?>) PulseBlasterSpecialRenderer.Unbaked.CODEC
        );

        // ── HUD overlay ────────────────────────────────────────────────────────
        PulseBlasterHudOverlay.register();

        // ── Cylinder spin tick ─────────────────────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(
                client -> PulseBlasterCylinderState.tick()
        );
    }
}