package net.filipes.rituals.item.custom;

import net.filipes.rituals.item.ModToolMaterials;
import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class BlightspearItem extends Item implements RitualsTooltipStyle {

    public BlightspearItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.spear(
                material,
                1.15f,
                1.2f,
                0.4f,
                2.5f,
                9.0f,
                5.5f,
                5.1f,
                8.75f,
                4.6f
        ));
    }

    @Override
    public int getNameColor() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorTop() {
        return 0;
    }

    @Override
    public int getTooltipBorderColorBottom() {
        return 0;
    }

    @Override
    public int getTooltipBackgroundColor() {
        return 0;
    }
}
