package in.lakazatong.pcbmod.platform;

import in.lakazatong.pcbmod.PCBModCommon;
import in.lakazatong.pcbmod.platform.services.IRegistryHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        PCBModCommon.LOGGER.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
