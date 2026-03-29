package net.filipes.rituals.item;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab; // Mojang name for ItemGroup
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {

    public static final CreativeModeTab RITUALS_ITEMS = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Rituals.MOD_ID, "rituals_items"),
            FabricCreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.HANDLE))
                    .title(Component.translatable("itemgroup.rituals.rituals_items"))
                    .displayItems((parameters, output) -> {
                        // In 26.1 Mojang Mappings, the method is 'accept'
                        // Ensure ModItems and ModBlocks use net.minecraft.world.item.Item
                        // and net.minecraft.world.level.block.Block
                        output.accept(ModItems.HANDLE);
                        output.accept(ModBlocks.BLOCK_TEST);
                        output.accept(ModBlocks.RITUAL_PEDESTAL);
                        output.accept(ModBlocks.RAW_ROSEGOLD_BLOCK);
                        output.accept(ModBlocks.ROSEGOLD_BLOCK);
                        output.accept(ModItems.ROSEGOLD_INGOT);
                        output.accept(ModItems.ROSEGOLD_PICKAXE);
                        output.accept(ModItems.RAW_ROSEGOLD);
                    })
                    .build());

    public static void registerItemGroups() {
        Rituals.LOGGER.info("Registering creative tabs for " + Rituals.MOD_ID);
    }
}