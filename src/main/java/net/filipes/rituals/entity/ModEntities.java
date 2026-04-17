package net.filipes.rituals.entity;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.entity.custom.ElectricBoltEntity;
import net.filipes.rituals.entity.custom.PulseBlasterBeamEntity;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<PulseBlasterBeamEntity> PULSE_BLASTER_BEAM = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pulse_blaster_beam"),
            EntityType.Builder.<PulseBlasterBeamEntity>of(
                            (type, level) -> new PulseBlasterBeamEntity(
                                    (EntityType<? extends PulseBlasterBeamEntity>) type, level),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "pulse_blaster_beam")
                    ))
    );
    public static final EntityType<ScreenShakeEntity> SCREEN_SHAKE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "screen_shake"),
            EntityType.Builder.<ScreenShakeEntity>of(
                            (type, level) -> new ScreenShakeEntity(
                                    (EntityType<? extends ScreenShakeEntity>) type,
                                    level
                            ),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "screen_shake")
                    ))
    );
    public static final EntityType<ElectricBoltEntity> ELECTRIC_BOLT = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "electric_bolt"),
            EntityType.Builder.<ElectricBoltEntity>of(
                            (type, level) -> new ElectricBoltEntity(
                                    (EntityType<? extends ElectricBoltEntity>) type,
                                    level
                            ),
                            MobCategory.MISC
                    )
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "electric_bolt")
                    ))
    );



    public static void registerModEntities() {
        Rituals.LOGGER.info("Registering Mod Entities for " + Rituals.MOD_ID);
    }
}