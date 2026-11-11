package dev.lilt.compat;

import dev.lilt.LiltMod;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ModCompat {

    private static final String[] KNOWN_MODS = {
        "sodium", "iris", "lithium", "ferritecore",
        "immediatelyfast", "entityculling", "moreculling"
    };

    private final Map<String, Boolean> detectedMods = new LinkedHashMap<>();

    public void detectInstalledMods() {
        FabricLoader loader = FabricLoader.getInstance();
        for (String modId : KNOWN_MODS) {
            boolean present = loader.isModLoaded(modId);
            detectedMods.put(modId, present);
            if (present) {
                LiltMod.LOGGER.info("Detected mod: {}", modId);
            }
        }
    }

    public boolean isModLoaded(String modId) {
        return detectedMods.getOrDefault(modId, false);
    }

    public boolean isSodiumLoaded() { return isModLoaded("sodium"); }
    public boolean isEntityCullingLoaded() { return isModLoaded("entityculling"); }
    public boolean isImmediatelyFastLoaded() { return isModLoaded("immediatelyfast"); }

    public Set<String> getDetectedModIds() {
        return Collections.unmodifiableSet(detectedMods.keySet());
    }

    public Map<String, Boolean> getAllDetectedMods() {
        return Collections.unmodifiableMap(detectedMods);
    }

    public boolean isParticleOptimizationHandledExternally() {
        return false;
    }
}
