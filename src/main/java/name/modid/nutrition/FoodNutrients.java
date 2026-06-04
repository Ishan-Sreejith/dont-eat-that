package name.modid.nutrition;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class FoodNutrients {
    private static final Map<Item, EnumMap<NutrientType, Float>> PROFILES = new HashMap<>();

    static {
        // Vitamin A rich foods
        register(Items.CARROT, nutrients(1.0f, 0f, 0f, 0f, 0f, 0f));
        register(Items.GOLDEN_CARROT, nutrients(1.5f, 0f, 0f, 0f, 0f, 0f));
        register(Items.MELON_SLICE, nutrients(0.8f, 0f, 0f, 0f, 0f, 0f));
        register(Items.PUMPKIN_PIE, nutrients(0.5f, 0f, 0f, 0.4f, 0f, 0.2f));
        register(name.modid.registry.ModItems.MANGO, nutrients(0.8f, 0f, 0.2f, 0f, 0f, 0f));

        // Vitamin B6 rich foods
        register(Items.POTATO, nutrients(0f, 1.0f, 0f, 0f, 0f, 0f));
        register(Items.BAKED_POTATO, nutrients(0f, 1.2f, 0f, 0f, 0f, 0f));
        register(name.modid.registry.ModItems.BANANA, nutrients(0f, 1.0f, 0f, 0f, 0f, 0f));
        register(Items.BEETROOT, nutrients(0f, 1.0f, 0f, 0f, 0f, 0f));
        register(Items.RABBIT_STEW, nutrients(0f, 0.8f, 0f, 0.3f, 0f, 0.3f));

        // Vitamin C rich foods
        register(Items.APPLE, nutrients(0f, 0f, 1.0f, 0f, 0f, 0f));
        register(name.modid.registry.ModItems.BIG_APPLE, nutrients(0.2f, 0f, 1.4f, 0f, 0f, 0f));
        register(name.modid.registry.ModItems.ORANGE, nutrients(0f, 0f, 1.5f, 0f, 0f, 0f));
        register(Items.SWEET_BERRIES, nutrients(0f, 0f, 1.0f, 0f, 0f, 0f));
        register(Items.GLOW_BERRIES, nutrients(0f, 0f, 1.1f, 0f, 0f, 0f));
        register(name.modid.registry.ModItems.STRAWBERRY, nutrients(0f, 0f, 1.0f, 0f, 0f, 0f));
        register(name.modid.registry.ModItems.BLACKBERRY, nutrients(0f, 0f, 0.8f, 0f, 0.2f, 0f));
        register(name.modid.registry.ModItems.RASPBERRY, nutrients(0f, 0f, 0.8f, 0f, 0.2f, 0f));
        register(name.modid.registry.ModItems.MULBERRY, nutrients(0f, 0f, 0.7f, 0f, 0.2f, 0.1f));
        register(name.modid.registry.ModItems.GRAPES, nutrients(0f, 0f, 0.7f, 0.2f, 0f, 0.1f));

        // Vitamin E rich foods
        register(Items.BEETROOT_SOUP, nutrients(0f, 1.2f, 0f, 0f, 0f, 0f));
        register(Items.SUNFLOWER, nutrients(0f, 0f, 0f, 0.8f, 0f, 0f));
        register(Items.WHEAT_SEEDS, nutrients(0f, 0f, 0f, 0.6f, 0f, 0.2f));
        register(Items.PUMPKIN_SEEDS, nutrients(0f, 0f, 0f, 0.8f, 0f, 0.2f));
        register(Items.MELON_SEEDS, nutrients(0f, 0f, 0f, 0.7f, 0f, 0.2f));
        register(Items.BEETROOT_SEEDS, nutrients(0f, 0f, 0f, 0.7f, 0f, 0.2f));

        // Vitamin K rich foods
        register(Items.DANDELION, nutrients(0f, 0f, 0f, 0f, 1.2f, 0f));
        register(Items.KELP, nutrients(0f, 0f, 0f, 0f, 0.8f, 0f));
        register(Items.DRIED_KELP, nutrients(0f, 0f, 0f, 0f, 1.0f, 0f));
        register(Items.SEA_PICKLE, nutrients(0f, 0f, 0f, 0.2f, 0.8f, 0f));
        register(Items.ALLIUM, nutrients(0.2f, 0f, 0.2f, 0f, 0.8f, 0f));

        // Fiber rich foods (bread gives half icon as requested)
        register(Items.BREAD, nutrients(0f, 0f, 0f, 0f, 0f, 0.5f));
        register(Items.COOKIE, nutrients(0f, 0f, 0f, 0f, 0f, 0.4f));
        register(Items.CAKE, nutrients(0f, 0f, 0f, 0f, 0f, 0.6f));
        register(Items.HONEY_BOTTLE, nutrients(0f, 0f, 0.3f, 0f, 0f, 0.2f));
        register(Items.HONEYCOMB, nutrients(0f, 0f, 0.2f, 0f, 0f, 0.3f));
        register(name.modid.registry.ModItems.FIBER, nutrients(0f, 0f, 0f, 0f, 0f, 0.5f));
        register(Items.CACTUS, nutrients(0f, 0f, 0f, 0f, 0f, 0f, 0.5f));

        // Neutral or mixed fallback foods
        register(Items.MUSHROOM_STEW, nutrients(0.2f, 0.3f, 0.2f, 0.2f, 0.2f, 0.2f, 0f));
        register(Items.SUSPICIOUS_STEW, nutrients(0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0f));
        register(Items.CHORUS_FRUIT, nutrients(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0f));
    }

    private FoodNutrients() {
    }

    public static EnumMap<NutrientType, Float> forStack(ItemStack stack) {
        EnumMap<NutrientType, Float> nutrients = PROFILES.get(stack.getItem());
        if (nutrients == null) {
            return new EnumMap<>(NutrientType.class);
        }
        return new EnumMap<>(nutrients);
    }

    public static boolean isRawMeat(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.BEEF || item == Items.CHICKEN || item == Items.PORKCHOP || item == Items.MUTTON || item == Items.RABBIT || item == Items.COD || item == Items.SALMON;
    }

    public static boolean isDesperationFood(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.WHEAT_SEEDS || item == Items.PUMPKIN_SEEDS || item == Items.MELON_SEEDS || item == Items.BEETROOT_SEEDS;
    }

    private static void register(Item item, EnumMap<NutrientType, Float> nutrients) {
        PROFILES.put(item, nutrients);
    }

    private static EnumMap<NutrientType, Float> nutrients(float vitaminA, float vitaminB6, float vitaminC, float vitaminE, float vitaminK, float fiber) {
        return nutrients(vitaminA, vitaminB6, vitaminC, vitaminE, vitaminK, fiber, 0f);
    }

    private static EnumMap<NutrientType, Float> nutrients(float vitaminA, float vitaminB6, float vitaminC, float vitaminE, float vitaminK, float fiber, float spiky) {
        EnumMap<NutrientType, Float> values = new EnumMap<>(NutrientType.class);
        values.put(NutrientType.VITAMIN_A, vitaminA);
        values.put(NutrientType.VITAMIN_B6, vitaminB6);
        values.put(NutrientType.VITAMIN_C, vitaminC);
        values.put(NutrientType.VITAMIN_E, vitaminE);
        values.put(NutrientType.VITAMIN_K, vitaminK);
        values.put(NutrientType.FIBER, fiber);
        values.put(NutrientType.SPIKY, spiky);
        return values;
    }
}
