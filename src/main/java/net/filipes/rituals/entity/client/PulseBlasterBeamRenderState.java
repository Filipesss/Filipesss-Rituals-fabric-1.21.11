package net.filipes.rituals.entity.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

// Package names changed for EntityRenderState
public class PulseBlasterBeamRenderState extends EntityRenderState {
    public float yaw;
    public float pitch;
    public boolean hasVelocity;
    // Note: Light and other default vars are already handled by EntityRenderState in Mojang mappings
}