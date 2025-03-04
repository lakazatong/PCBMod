package in.lakazatong.pcbmod.registries;

import in.lakazatong.pcbmod.platform.Services;
import in.lakazatong.pcbmod.registries.datacomponents.LastPosDataComponent;
import in.lakazatong.pcbmod.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.component.DataComponentType;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final Supplier<DataComponentType<UUIDDataComponent>> UUID = Services.REGISTRY.registerDataComponent("uuid", UUIDDataComponent::getBuilder);
    public static final Supplier<DataComponentType<LastPosDataComponent>> LAST_POS = Services.REGISTRY.registerDataComponent("last_pos", LastPosDataComponent::getBuilder);

    public static void trigger() { }
}
