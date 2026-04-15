package net.filipes.rituals.item;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.util.ModTags;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public class ModArmorMaterials {

    public static final ResourceKey<EquipmentAsset> ROSEGOLD_EQUIPMENT_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "rosegold")
    );

    public static final ArmorMaterial ROSEGOLD = new ArmorMaterial(
            15,                             // durability multiplier (gold = 7, iron = 15)
            Map.of(
                    ArmorType.HELMET,     3,
                    ArmorType.CHESTPLATE, 8,
                    ArmorType.LEGGINGS,   6,
                    ArmorType.BOOTS,      3
            ),
            15,                             // enchantability (matches gold)
            SoundEvents.ARMOR_EQUIP_GOLD,
            3.0f,                           // toughness
            0.0f,                           // knockback resistance
            ModTags.Items.ROSEGOLD_REPAIR,  // TagKey<Item>, same tag as ToolMaterial
            ROSEGOLD_EQUIPMENT_ASSET
    );
}