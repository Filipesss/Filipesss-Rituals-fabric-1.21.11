package net.filipes.rituals.blocks;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.blocks.custom.RitualPedestalBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {

    public static final Block BLOCK_TEST = registerBlock("block_test",
            BlockBehaviour.Properties.of()
                    .strength(4f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST));

    public static final Block ROSEGOLD_BLOCK = registerBlock("rosegold_block",
            BlockBehaviour.Properties.of()
                    .strength(4f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST));

    public static final Block RAW_ROSEGOLD_BLOCK = registerBlock("raw_rosegold_block",
            BlockBehaviour.Properties.of()
                    .strength(4f)
                    .requiresCorrectToolForDrops()
                    .sound(SoundType.AMETHYST));

    public static final RitualPedestalBlock RITUAL_PEDESTAL = registerRitualPedestal("ritual_pedestal",
            BlockBehaviour.Properties.of()
                    .strength(-1f, 3600000f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    private static RitualPedestalBlock registerRitualPedestal(String name, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Rituals.MOD_ID, name));

        RitualPedestalBlock block = new RitualPedestalBlock(properties.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        registerBlockItem(name, block);

        return block;
    }

    private static Block registerBlock(String name, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(
                net.minecraft.core.registries.Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Rituals.MOD_ID, name));

        Block block = new Block(properties.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        registerBlockItem(name, block);

        return block;
    }

    private static void registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(
                net.minecraft.core.registries.Registries.ITEM,
                Identifier.fromNamespaceAndPath(Rituals.MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void registerModBlocks() {
        Rituals.LOGGER.info("Registering mod blocks for " + Rituals.MOD_ID);
    }
}