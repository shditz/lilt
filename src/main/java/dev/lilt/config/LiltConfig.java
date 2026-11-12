package dev.lilt.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.lilt.LiltMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LiltConfig {

    private static final int SCHEMA_VERSION = 2;
    private static final String CONFIG_FILE = "lilt.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private int schemaVersion = SCHEMA_VERSION;
    private String activeProfile = "balanced";
    private boolean overlayEnabled = false;
    private boolean adaptiveEnabled = true;
    private boolean particleOptimizationEnabled = true;
    private int particleBudget = 1000;
    private double particleDistanceCullSq = 1024.0;
    private boolean animationLodEnabled = true;
    private boolean textureCullingEnabled = true;
    private boolean tickBurstProtectionEnabled = true;
    private boolean gcGuardEnabled = true;
    private int monitorWindowSize = 600;
    private boolean diagnosticsEnabled = true;
    private double adaptiveHysteresisLow = 0.85;
    private double adaptiveHysteresisHigh = 0.95;
    private int adaptiveCooldownTicks = 100;

    private transient Path configPath;

    public LiltConfig() {
        applyProfile();
    }

    public static LiltConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        return load(configDir.resolve(CONFIG_FILE));
    }

    public static LiltConfig load(Path configPath) {
        LiltConfig config;
        if (Files.exists(configPath)) {
            try {
                String json = Files.readString(configPath);
                config = GSON.fromJson(json, LiltConfig.class);
                if (config == null) {
                    LiltMod.LOGGER.warn("Config file was empty, using defaults");
                    config = new LiltConfig();
                } else if (config.schemaVersion != SCHEMA_VERSION) {
                    LiltMod.LOGGER.warn("Config schema version mismatch (found {}, expected {}), migrating",
                        config.schemaVersion, SCHEMA_VERSION);
                    config = migrate(config);
                }
            } catch (JsonSyntaxException e) {
                LiltMod.LOGGER.error("Invalid config file, using defaults", e);
                config = new LiltConfig();
                try {
                    Path backup = configPath.resolveSibling("lilt.json.broken");
                    Files.copy(configPath, backup);
                } catch (IOException ignored) {
                }
            } catch (IOException e) {
                LiltMod.LOGGER.error("Failed to read config file, using defaults", e);
                config = new LiltConfig();
            }
        } else {
            LiltMod.LOGGER.info("No config file found, creating defaults");
            config = new LiltConfig();
        }

        config.configPath = configPath;
        config.validate();
        config.save();
        return config;
    }

    private static LiltConfig migrate(LiltConfig old) {
        LiltConfig migrated = new LiltConfig();
        migrated.activeProfile = old.activeProfile;
        migrated.overlayEnabled = old.overlayEnabled;
        migrated.adaptiveEnabled = old.adaptiveEnabled;
        migrated.particleOptimizationEnabled = old.particleOptimizationEnabled;
        migrated.particleBudget = old.particleBudget;
        migrated.particleDistanceCullSq = old.particleDistanceCullSq;
        migrated.animationLodEnabled = old.animationLodEnabled;
        migrated.textureCullingEnabled = old.textureCullingEnabled;
        migrated.tickBurstProtectionEnabled = old.tickBurstProtectionEnabled;
        migrated.gcGuardEnabled = old.gcGuardEnabled;
        return migrated;
    }

    private void validate() {
        if (activeProfile == null || activeProfile.isBlank()) {
            activeProfile = "balanced";
        }
        String lower = activeProfile.toLowerCase();
        if (!lower.equals("low") && !lower.equals("balanced") && !lower.equals("performance") && !lower.equals("custom")) {
            LiltMod.LOGGER.warn("Unknown profile '{}', falling back to balanced", activeProfile);
            activeProfile = "balanced";
        }
        activeProfile = lower;

        particleBudget = Math.max(50, Math.min(particleBudget, 10000));
        particleDistanceCullSq = Math.max(64.0, Math.min(particleDistanceCullSq, 16384.0));
        monitorWindowSize = Math.max(60, Math.min(monitorWindowSize, 1200));
        adaptiveHysteresisLow = Math.max(0.5, Math.min(adaptiveHysteresisLow, 0.99));
        adaptiveHysteresisHigh = Math.max(adaptiveHysteresisLow + 0.01, Math.min(adaptiveHysteresisHigh, 1.0));
        adaptiveCooldownTicks = Math.max(20, Math.min(adaptiveCooldownTicks, 600));
    }

    private void applyProfile() {
        switch (activeProfile) {
            case "low" -> {
                particleBudget = 300;
                particleDistanceCullSq = 400.0;
                animationLodEnabled = true;
                textureCullingEnabled = true;
                tickBurstProtectionEnabled = true;
                gcGuardEnabled = true;
            }
            case "balanced" -> {
                particleBudget = 1000;
                particleDistanceCullSq = 1024.0;
                animationLodEnabled = true;
                textureCullingEnabled = true;
                tickBurstProtectionEnabled = true;
                gcGuardEnabled = true;
            }
            case "performance" -> {
                particleBudget = 2500;
                particleDistanceCullSq = 4096.0;
                animationLodEnabled = true;
                textureCullingEnabled = true;
                tickBurstProtectionEnabled = true;
                gcGuardEnabled = true;
            }
        }
    }

    public void save() {
        if (configPath == null) return;
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            LiltMod.LOGGER.error("Failed to save config", e);
        }
    }

    public String getActiveProfile() { return activeProfile; }
    public boolean isOverlayEnabled() { return overlayEnabled; }
    public boolean isAdaptiveEnabled() { return adaptiveEnabled; }
    public boolean isParticleOptimizationEnabled() { return particleOptimizationEnabled; }
    public int getParticleBudget() { return particleBudget; }
    public double getParticleDistanceCullSq() { return particleDistanceCullSq; }
    public boolean isAnimationLodEnabled() { return animationLodEnabled; }
    public boolean isTextureCullingEnabled() { return textureCullingEnabled; }
    public boolean isTickBurstProtectionEnabled() { return tickBurstProtectionEnabled; }
    public boolean isGcGuardEnabled() { return gcGuardEnabled; }
    public int getMonitorWindowSize() { return monitorWindowSize; }
    public boolean isDiagnosticsEnabled() { return diagnosticsEnabled; }
    public double getAdaptiveHysteresisLow() { return adaptiveHysteresisLow; }
    public double getAdaptiveHysteresisHigh() { return adaptiveHysteresisHigh; }
    public int getAdaptiveCooldownTicks() { return adaptiveCooldownTicks; }

    public void setActiveProfile(String profile) {
        this.activeProfile = profile;
        applyProfile();
        validate();
        save();
    }

    public void setOverlayEnabled(boolean enabled) {
        this.overlayEnabled = enabled;
        save();
    }

    public void setAdaptiveEnabled(boolean enabled) {
        this.adaptiveEnabled = enabled;
        save();
    }

    public void setParticleBudget(int budget) {
        this.particleBudget = budget;
        this.activeProfile = "custom";
        validate();
        save();
    }

    public void setParticleDistanceCullSq(double distSq) {
        this.particleDistanceCullSq = distSq;
        this.activeProfile = "custom";
        validate();
        save();
    }

    public void setParticleOptimizationEnabled(boolean enabled) {
        this.particleOptimizationEnabled = enabled;
        save();
    }

    public void setAnimationLodEnabled(boolean enabled) {
        this.animationLodEnabled = enabled;
        this.activeProfile = "custom";
        save();
    }

    public void setTextureCullingEnabled(boolean enabled) {
        this.textureCullingEnabled = enabled;
        this.activeProfile = "custom";
        save();
    }

    public void setTickBurstProtectionEnabled(boolean enabled) {
        this.tickBurstProtectionEnabled = enabled;
        this.activeProfile = "custom";
        save();
    }

    public void setGcGuardEnabled(boolean enabled) {
        this.gcGuardEnabled = enabled;
        this.activeProfile = "custom";
        save();
    }
}