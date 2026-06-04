package name.modid.registry;

import name.modid.DontEatThat;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item FIBER = register("fiber", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).alwaysEdible().build()).maxCount(16)));
    public static final Item ORANGE = register("orange", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(0.2f).build())));
    public static final Item BANANA = register("banana", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(0.2f).build())));
    public static final Item MANGO = register("mango", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(0.2f).build())));
    public static final Item STRAWBERRY = register("strawberry", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).build())));
    public static final Item BLACKBERRY = register("blackberry", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).build())));
    public static final Item RASPBERRY = register("raspberry", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).build())));
    public static final Item MULBERRY = register("mulberry", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(1).saturationModifier(0.1f).build())));
    public static final Item BIG_APPLE = register("big_apple", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(4).saturationModifier(0.4f).build())));
    public static final Item GRAPES = register("grapes", new Item(new Item.Settings().food(new FoodComponent.Builder().hunger(2).saturationModifier(0.2f).build())));

    private ModItems() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> entries.add(FIBER));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(ORANGE);
            entries.add(BANANA);
            entries.add(MANGO);
            entries.add(STRAWBERRY);
            entries.add(BLACKBERRY);
            entries.add(RASPBERRY);
            entries.add(MULBERRY);
            entries.add(BIG_APPLE);
            entries.add(GRAPES);
        });
    }

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(DontEatThat.MOD_ID, name), item);
    }
}
