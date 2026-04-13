package net.filipes.rituals.item.custom;

import net.filipes.rituals.item.ModArmorMaterials;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class RosegoldHelmetItem extends Item implements RitualsTooltipStyle {
    public RosegoldHelmetItem(ArmorMaterial material, Properties settings) {
        super(settings
                .humanoidArmor(material, ArmorType.HELMET)
                .durability(ArmorType.HELMET.getDurability(2)));
    }

    @Override public int getNameColor() { return 0xFFB6C1; }
    @Override public int getTooltipBorderColorTop() { return 0xFFB6C1; }
    @Override public int getTooltipBorderColorBottom() { return 0xFF69B4; }
    @Override public int getTooltipBackgroundColor() { return 0xFF1A0010; }
}