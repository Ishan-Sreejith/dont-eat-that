package name.modid.registry;

import name.modid.DontEatThat;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModBlocks {
    public static final Block SEED_BLOCK = registerBlock("seed_block",
            new Block(AbstractBlock.Settings.create().mapColor(MapColor.OAK_TAN).strength(0.8f).pistonBehavior(PistonBehavior.DESTROY)));

    private ModBlocks() {
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register(entries -> entries.add(SEED_BLOCK));
    }

    private static Block registerBlock(String name, Block block) {
        Registry.register(Registries.ITEM, new Identifier(DontEatThat.MOD_ID, name), new BlockItem(block, new Item.Settings()));
        return Registry.register(Registries.BLOCK, new Identifier(DontEatThat.MOD_ID, name), block);
    }
}
