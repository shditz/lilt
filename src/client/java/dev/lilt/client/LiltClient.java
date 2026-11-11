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

        config = LiltConfig.load();
        LiltMod.LOGGER.info("Configuration loaded: profile={}", config.getActiveProfile());

        hardwareInfo = new HardwareInfo();
        LiltMod.LOGGER.info("Hardware detected: {} CPU cores, max heap: {} MB",
            hardwareInfo.getLogicalProcessors(), hardwareInfo.getMaxHeapMb());

        monitor = new PerformanceMonitor(config);
        diagnostics = new PerformanceDiagnostics(monitor);

        modCompat = new ModCompat();
        modCompat.detectInstalledMods();

        particleOptimizer = new ParticleOptimizer(config, modCompat);
        adaptiveController = new AdaptiveController(config, monitor, diagnostics, particleOptimizer);
        overlayRenderer = new OverlayRenderer(config, monitor, diagnostics, hardwareInfo);

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("lilt", "performance_overlay"), overlayRenderer);
        KeyBindings.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && !inGameInitialized) {
                inGameInitialized = true;
                try {
                    com.mojang.renderpearl.api.device.GpuDevice device = com.mojang.blaze3d.systems.RenderSystem.tryGetDevice();
                    if (device != null) {
                        com.mojang.renderpearl.api.device.DeviceInfo info = device.getDeviceInfo();
                        if (info != null) {
                            hardwareInfo.setGpuInfo(info.vendorName(), info.name(), info.backendName());
                        }
                    }
                } catch (Throwable t) {
                    LiltMod.LOGGER.warn("Could not query GPU info via RenderPearl: {}", t.getMessage());
                }
                LiltMod.LOGGER.info("Lilt active. GPU: {} (Backend: {})",
                    hardwareInfo.getGpuRenderer(), hardwareInfo.getGraphicsBackend());
            }

            if (inGameInitialized) {
                if (client.hasSingleplayerServer() && client.getSingleplayerServer() != null) {
                    long avgNanos = client.getSingleplayerServer().getAverageTickTimeNanos();
                    monitor.recordIntegratedServerTick(avgNanos);
                } else {
                    monitor.setRemoteServer();
                }

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
