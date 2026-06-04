package name.modid.registry;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.util.Identifier;

public final class ModLootTables {
    private static final Identifier OAK_LEAVES = new Identifier("minecraft", "blocks/oak_leaves");
    private static final Identifier BIRCH_LEAVES = new Identifier("minecraft", "blocks/birch_leaves");
    private static final Identifier SPRUCE_LEAVES = new Identifier("minecraft", "blocks/spruce_leaves");
    private static final Identifier ACACIA_LEAVES = new Identifier("minecraft", "blocks/acacia_leaves");
    private static final Identifier DARK_OAK_LEAVES = new Identifier("minecraft", "blocks/dark_oak_leaves");
    private static final Identifier JUNGLE_LEAVES = new Identifier("minecraft", "blocks/jungle_leaves");
    private static final Identifier VINE = new Identifier("minecraft", "blocks/vine");
    private static final Identifier GRASS = new Identifier("minecraft", "blocks/grass");

    private ModLootTables() {
    }

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(OAK_LEAVES)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.ORANGE).weight(3))
                        .with(ItemEntry.builder(ModItems.MANGO).weight(2))
                        .with(ItemEntry.builder(Items.APPLE).weight(5))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                );
            } else if (id.equals(BIRCH_LEAVES)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.ORANGE).weight(5))
                        .with(ItemEntry.builder(ModItems.MANGO).weight(5))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                );
            } else if (id.equals(SPRUCE_LEAVES)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.MANGO).weight(10))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                );
            } else if (id.equals(ACACIA_LEAVES)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.ORANGE).weight(10))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                );
            } else if (id.equals(DARK_OAK_LEAVES)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.MULBERRY).weight(6))
                        .with(ItemEntry.builder(ModItems.BIG_APPLE).weight(4))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                );
            } else if (id.equals(JUNGLE_LEAVES)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(Items.COCOA_BEANS).weight(10))
                        .conditionally(RandomChanceLootCondition.builder(0.05f))
                );
            } else if (id.equals(VINE)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.GRAPES).weight(10))
                        .conditionally(RandomChanceLootCondition.builder(0.10f))
                );
            } else if (id.equals(GRASS)) {
                tableBuilder.pool(LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(ModItems.STRAWBERRY).weight(3))
                        .with(ItemEntry.builder(ModItems.BLACKBERRY).weight(3))
                        .with(ItemEntry.builder(ModItems.RASPBERRY).weight(3))
                        .conditionally(RandomChanceLootCondition.builder(0.25f))
                );
            }
        });
    }
}
