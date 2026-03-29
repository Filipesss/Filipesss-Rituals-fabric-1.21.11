package net.filipes.rituals.network;

// RegistryByteBuf → RegistryFriendlyByteBuf
import net.minecraft.network.RegistryFriendlyByteBuf;
// PacketCodec → StreamCodec
import net.minecraft.network.codec.StreamCodec;
// PacketCodecs → ByteBufCodecs
import net.minecraft.network.codec.ByteBufCodecs;
// CustomPayload → CustomPacketPayload
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PulseBlasterAmmoPayload(int ammo) implements CustomPacketPayload {

    // CustomPayload.Id → CustomPacketPayload.Type
    public static final CustomPacketPayload.Type<PulseBlasterAmmoPayload> ID =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("rituals", "pulse_blaster_beam"));

    // PacketCodec → StreamCodec; RegistryByteBuf → RegistryFriendlyByteBuf
    public static final StreamCodec<RegistryFriendlyByteBuf, PulseBlasterAmmoPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    PulseBlasterAmmoPayload::ammo,
                    PulseBlasterAmmoPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() { return ID; } // getId() → type()
}