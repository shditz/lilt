package dev.lilt.ui;

import dev.lilt.config.LiltConfig;
import dev.lilt.diagnostics.PerformanceDiagnostics;
import dev.lilt.hardware.HardwareInfo;
import dev.lilt.monitor.PerformanceMonitor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Renders the performance overlay HUD.
 * Implements Fabric's HudElement for modern 26.3 rendering.
 */
public class OverlayRenderer implements HudElement {

    private final LiltConfig config;
    private final PerformanceMonitor monitor;
    private final PerformanceDiagnostics diagnostics;
    private final HardwareInfo hardwareInfo;

    private boolean visible = false;

    // Colors
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_GREEN = 0xFF55FF55;
    private static final int COLOR_YELLOW = 0xFFFFFF55;
    private static final int COLOR_RED = 0xFFFF5555;
    private static final int COLOR_GRAY = 0xFFAAAAAA;
    private static final int COLOR_BG = 0x90000000;

    // Layout
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;

    public OverlayRenderer(LiltConfig config, PerformanceMonitor monitor,
                           PerformanceDiagnostics diagnostics, HardwareInfo hardwareInfo) {
        this.config = config;
        this.monitor = monitor;
        this.diagnostics = diagnostics;
        this.hardwareInfo = hardwareInfo;
        this.visible = config.isOverlayEnabled();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!visible) return;

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        if (font == null) return;

        int x = PADDING;
        int y = PADDING;
        int lineWidth = 180;
        int lines = 12;

        // Background
        graphics.fill(x - 2, y - 2, x + lineWidth + 2, y + (lines * LINE_HEIGHT) + 2, COLOR_BG);

        // Title
        graphics.text(font, "Lilt Performance", x, y, COLOR_WHITE);
        y += LINE_HEIGHT;

        // FPS
        double fps = monitor.getCurrentFps();
        int fpsColor = fps >= 55 ? COLOR_GREEN : fps >= 30 ? COLOR_YELLOW : COLOR_RED;
        graphics.text(font, String.format("FPS: %.0f (avg: %.0f)", fps, monitor.getAverageFps()), x, y, fpsColor);
        y += LINE_HEIGHT;

        // Frame times
        graphics.text(font, String.format("Frame: %.1fms", monitor.getFrameTimeMs()), x, y, COLOR_GRAY);
        y += LINE_HEIGHT;

        graphics.text(font, String.format("P50: %.1fms  P95: %.1fms", monitor.getP50FrameTimeMs(), monitor.getP95FrameTimeMs()), x, y, COLOR_GRAY);
        y += LINE_HEIGHT;

        graphics.text(font, String.format("P99: %.1fms  1%% Low: %.0f", monitor.getP99FrameTimeMs(), monitor.getOnePercentLowFps()), x, y, COLOR_GRAY);
        y += LINE_HEIGHT;

        // Memory
        long usedMb = hardwareInfo.getUsedHeapMb();
        long maxMb = hardwareInfo.getMaxHeapMb();
        double memRatio = maxMb > 0 ? (double) usedMb / maxMb : 0;
        int memColor = memRatio < 0.7 ? COLOR_GREEN : memRatio < 0.85 ? COLOR_YELLOW : COLOR_RED;
        graphics.text(font, String.format("Heap: %dM / %dM (%.0f%%)", usedMb, maxMb, memRatio * 100), x, y, memColor);
        y += LINE_HEIGHT;

        // GC
        graphics.text(font, String.format("GC: %d collections (+%dms)", monitor.getGcCountDelta(), monitor.getGcTimeDeltaMs()), x, y, COLOR_GRAY);
        y += LINE_HEIGHT;

        // Tick
        double tickMs = monitor.getLastTickDurationMs();
        int tickColor = tickMs < 50 ? COLOR_GREEN : COLOR_RED;
        graphics.text(font, String.format("Tick: %.1fms", tickMs), x, y, tickColor);
        y += LINE_HEIGHT;

        // GPU
        graphics.text(font, "GPU: " + hardwareInfo.getGpuRenderer(), x, y, COLOR_GRAY);
        y += LINE_HEIGHT;

        // Diagnostics
        PerformanceDiagnostics.PressureType pressure = diagnostics.getCurrentPressure();
        int pressureColor = switch (pressure) {
            case NONE -> COLOR_GREEN;
            case UNKNOWN -> COLOR_GRAY;
            default -> COLOR_YELLOW;
        };
        graphics.text(font, "Status: " + pressure.getDisplayName(), x, y, pressureColor);
        y += LINE_HEIGHT;

        // Profile
        graphics.text(font, "Profile: " + config.getActiveProfile(), x, y, COLOR_WHITE);
        y += LINE_HEIGHT;

        // Adaptive
        if (config.isAdaptiveEnabled()) {
            graphics.text(font, "Adaptive: active", x, y, COLOR_GREEN);
        } else {
            graphics.text(font, "Adaptive: off", x, y, COLOR_GRAY);
        }
    }

    public void toggle() {
        visible = !visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

