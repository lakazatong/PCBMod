package in.lakazatong.pcbmod.registries;

import in.lakazatong.pcbmod.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

import java.util.function.Supplier;

public class ModTabs {
    public static final Supplier<CreativeModeTab> EXAMPLE_TAB = Services.REGISTRY.registerCreativeModeTab(
            "pcb_mod",
            Component.translatable("itemGroup.pcb_mod"),
            Items.GRASS_BLOCK::getDefaultInstance, () -> Items.GRASS_BLOCK);

    public static void trigger() { }
}
