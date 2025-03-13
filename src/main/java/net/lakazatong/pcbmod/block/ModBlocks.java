package net.lakazatong.pcbmod.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lakazatong.pcbmod.PCBMod;
import net.lakazatong.pcbmod.block.custom.PortBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import java.util.function.Function;

import static net.lakazatong.pcbmod.item.ModItems.MOD_TAB_KEY;

public class ModBlocks {
    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(MOD_TAB_KEY).register(itemGroup -> {
            itemGroup.add(ModBlocks.PORT);
        });
    }

    private static Block register(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = keyOfBlock(name);
        Block block = blockFactory.apply(settings.registryKey(blockKey));

        RegistryKey<Item> itemKey = keyOfItem(name);

        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, itemKey, blockItem);

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    private static RegistryKey<Block> keyOfBlock(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(PCBMod.MOD_ID, name));
    }

    private static RegistryKey<Item> keyOfItem(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(PCBMod.MOD_ID, name));
    }

    public static final Block PORT = register(
            "port",
            PortBlock::new,
            AbstractBlock.Settings.create().sounds(BlockSoundGroup.METAL)
    );
}