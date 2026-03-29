package net.filipes.rituals.item;

import net.filipes.rituals.Rituals;
import net.filipes.rituals.item.custom.LightningRapierItem;
import net.filipes.rituals.item.custom.PulseBlasterItem;
import net.filipes.rituals.item.custom.RosegoldPickaxeItem;

// DataComponentTypes → DataComponents
import net.minecraft.core.component.DataComponents;

// Registry/Registries/RegistryKey split into two packages in Mojang
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;  // was: Registries (instance registry)
import net.minecraft.core.registries.Registries;         // was: RegistryKeys (key registry)

// Identifier → ResourceLocation; RegistryKey → ResourceKey
import net.minecraft.resources.Identifier;

import net.minecraft.resources.ResourceKey;


// class_3902 (intermediary) → Unbreakable

// LoreComponent → ItemLore
import net.minecraft.util.Unit;
import net.minecraft.world.item.component.ItemLore;

// Text → Component; TextColor stays but moves to network.chat
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

import net.minecraft.world.item.Item;

import java.util.List;
import java.util.function.Function;

public class ModItems {

    public static final Item HANDLE = registerItem("handle",
            settings -> new Item(settings),
            new Item.Properties().component(DataComponents.LORE,
                    new ItemLore(List.of(Component.translatable("tooltip.rituals.handle")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))))));

    public static final Item ROSEGOLD_INGOT = registerItem("rosegold_ingot",
            settings -> new Item(settings),
            new Item.Properties().component(DataComponents.LORE,
                    new ItemLore(List.of(Component.translatable("tooltip.rituals.rosegold")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))))));

    public static final Item RAW_ROSEGOLD = registerItem("raw_rosegold",
            settings -> new Item(settings),
            new Item.Properties().component(DataComponents.LORE,
                    new ItemLore(List.of(Component.translatable("tooltip.rituals.raw_rosegold")
                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF)).withItalic(false))))));

    public static final Item EM_PICK = registerItem("em_pick",
            settings -> new Item(settings),
            // fireproof() → fireResistant()
            new Item.Properties().pickaxe(ModToolMaterials.ROSEGOLD, 1.0F, -2.8F).fireResistant());

    public static final Item ROSEGOLD_PICKAXE = registerItem("rosegold_pickaxe",
            settings -> new RosegoldPickaxeItem(ModToolMaterials.ROSEGOLD, 1.0F, -2.8F, settings),
            new Item.Properties()
                    // class_3902.field_17274 → new Unbreakable(false)
                    // false = don't show the "Unbreakable" tooltip line; flip to true if you want it shown
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                    .fireResistant()
                    .component(DataComponents.LORE,
                            new ItemLore(List.of(Component.translatable("tooltip.rituals.handle")
                                    .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFB6C1)).withItalic(false))))));

    public static final Item LIGHTNING_RAPIER = registerItem("lightning_rapier",
            settings -> new LightningRapierItem(ModToolMaterials.ROSEGOLD, 1.5F, -1.8F, settings),
            new Item.Properties()
                    .stacksTo(1)                                           // maxCount() → stacksTo()
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                    .component(DataComponents.LORE,
                            new ItemLore(List.of(
                                    Component.translatable("tooltip.rituals.lightning_rapier")
                                            .withStyle(style -> style.withColor(TextColor.fromRgb(0x9B6DFF)).withItalic(false))
                            ))));

    public static final Item PULSE_BLASTER = registerItem("pulse_blaster",
            settings -> new PulseBlasterItem(settings),
            new Item.Properties()
                    .stacksTo(1)
                    .fireResistant()
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)
                    .component(DataComponents.LORE,
                            new ItemLore(List.of(
                                    Component.translatable("tooltip.rituals.pulse_blaster")
                                            .withStyle(style -> style.withColor(TextColor.fromRgb(0xFF2626)).withItalic(false))
                            ))));

    private static Item registerItem(String name, Function<Item.Properties, Item> creator, Item.Properties settings) {
        // Identifier.of() → ResourceLocation.fromNamespaceAndPath()
        Identifier id = Identifier.fromNamespaceAndPath(Rituals.MOD_ID, name);
        // RegistryKey.of(RegistryKeys.ITEM, ...) → ResourceKey.create(Registries.ITEM, ...)
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        settings = settings.setId(key);            // registryKey() → setId()
        Item item = creator.apply(settings);
        // Registry.register(Registries.ITEM, ...) → Registry.register(BuiltInRegistries.ITEM, ...)
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void registerModItems() {
        Rituals.LOGGER.info("Registering ModItems");
    }
}