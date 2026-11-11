package dev.lilt.client;

import dev.lilt.LiltMod;
import dev.lilt.adaptive.AdaptiveController;
import dev.lilt.compat.ModCompat;
import dev.lilt.config.LiltConfig;
import dev.lilt.diagnostics.PerformanceDiagnostics;
import dev.lilt.hardware.HardwareInfo;
import dev.lilt.monitor.PerformanceMonitor;
import dev.lilt.optimization.particle.ParticleOptimizer;
import dev.lilt.ui.KeyBindings;
import dev.lilt.ui.OverlayRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

/**
 * Client-side entry point for Lilt.
 * Initializes all subsystems in the correct order.
 */
public class LiltClient implements ClientModInitializer {

    private static LiltConfig config;
    private static HardwareInfo hardwareInfo;
    private static PerformanceMonitor monitor;
    private static PerformanceDiagnostics diagnostics;
    private static AdaptiveController adaptiveController;
    private static ParticleOptimizer particleOptimizer;
    private static ModCompat modCompat;
    private static OverlayRenderer overlayRenderer;
    private static boolean inGameInitialized = false;

    @Override
    public void onInitializeClient() {
        LiltMod.LOGGER.info("Lilt client initialization starting");

        // 1. Load configuration
        config = LiltConfig.load();
        LiltMod.LOGGER.info("Configuration loaded: profile={}", config.getActiveProfile());

        // 2. Detect hardware baseline
        hardwareInfo = new HardwareInfo();
        LiltMod.LOGGER.info("Hardware detected: {} CPU cores, max heap: {} MB",
            hardwareInfo.getLogicalProcessors(), hardwareInfo.getMaxHeapMb());

        // 3. Initialize performance monitor
        monitor = new PerformanceMonitor(config);

        // 4. Initialize diagnostics engine
        diagnostics = new PerformanceDiagnostics(monitor);

        // 5. Detect compatible and installed mods
        modCompat = new ModCompat();
        modCompat.detectInstalledMods();

        // 6. Initialize particle optimizer
        particleOptimizer = new ParticleOptimizer(config, modCompat);

        // 7. Initialize adaptive controller
        adaptiveController = new AdaptiveController(config, monitor, diagnostics, particleOptimizer);

        // 8. Initialize overlay renderer
        overlayRenderer = new OverlayRenderer(config, monitor, diagnostics, hardwareInfo);

        // 9. Register HUD element with Fabric API
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("lilt", "performance_overlay"), overlayRenderer);

        // 10. Register keybindings
        KeyBindings.register();

        // 11. Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !inGameInitialized) {
                inGameInitialized = true;
                hardwareInfo.queryGpuInfo();
                LiltMod.LOGGER.info("Lilt fully active in-game. GPU: {}", hardwareInfo.getGpuRenderer());
            }

            if (inGameInitialized) {
                monitor.tick();
                diagnostics.update();

                if (config.isAdaptiveEnabled()) {
                    adaptiveController.tick();
                }
            }

            KeyBindings.handleInput(config, overlayRenderer);
        });

        LiltMod.LOGGER.info("Lilt client initialization complete");
    }

    public static LiltConfig getConfig() {
        return config;
    }

    public static PerformanceMonitor getMonitor() {
        return monitor;
    }

    public static HardwareInfo getHardwareInfo() {
        return hardwareInfo;
    }

    public static PerformanceDiagnostics getDiagnostics() {
        return diagnostics;
    }

    public static ParticleOptimizer getParticleOptimizer() {
        return particleOptimizer;
    }

    public static ModCompat getModCompat() {
        return modCompat;
    }

    public static AdaptiveController getAdaptiveController() {
        return adaptiveController;
    }

    public static OverlayRenderer getOverlayRenderer() {
        return overlayRenderer;
    }
}
