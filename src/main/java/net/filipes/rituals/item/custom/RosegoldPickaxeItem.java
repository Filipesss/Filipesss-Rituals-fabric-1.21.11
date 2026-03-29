package net.filipes.rituals.item.custom;

import net.filipes.rituals.util.RitualsTooltipStyle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.phys.HitResult;
import java.util.ArrayList;
import java.util.List;

public class RosegoldPickaxeItem extends Item implements RitualsTooltipStyle {

    public RosegoldPickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.pickaxe(material, attackDamage, attackSpeed));
    }

    public static List<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initialBlockPos, ServerPlayer player) {
        List<BlockPos> positions = new ArrayList<>();
        HitResult hit = player.pick(20, 0, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return positions;
        }

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    positions.add(new BlockPos(
                            initialBlockPos.getX() + x,
                            initialBlockPos.getY() + y,
                            initialBlockPos.getZ() + z
                    ));
                }
            }
        }
        return positions;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(getNameColor()))
                        .withItalic(false));
    }

    @Override public int getNameColor()              { return 0xFFFFB6C1; }
    @Override public int getTooltipBorderColor()     { return 0xFFFF80AA; }
    @Override public int getTooltipBackgroundColor() { return 0xE51A0510; }
}