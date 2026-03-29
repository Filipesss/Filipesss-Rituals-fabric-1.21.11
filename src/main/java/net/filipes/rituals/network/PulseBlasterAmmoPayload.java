package net.filipes.rituals.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PulseBlasterAmmoPayload(int ammo) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PulseBlasterAmmoPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_beam"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PulseBlasterAmmoPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PulseBlasterAmmoPayload::ammo,
                    PulseBlasterAmmoPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; }
}