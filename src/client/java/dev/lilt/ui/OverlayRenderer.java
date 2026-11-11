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

public class OverlayRenderer implements HudElement {

    private final LiltConfig config;
    private final PerformanceMonitor monitor;
    private final PerformanceDiagnostics diagnostics;
    private final HardwareInfo hardwareInfo;

    private boolean visible = false;

    private static final int COLOR_BG = 0xD012151B;
    private static final int COLOR_BORDER = 0x503C4452;
    private static final int COLOR_HEADER = 0xFF64B5F6;
    private static final int COLOR_TEXT_PRIMARY = 0xFFF0F3F6;
    private static final int COLOR_TEXT_MUTED = 0xFF8A94A0;
    private static final int COLOR_GOOD = 0xFF81C784;
    private static final int COLOR_WARN = 0xFFFFD54F;
    private static final int COLOR_ALERT = 0xFFE57373;

    private static final int X = 6;
    private static final int Y = 6;
    private static final int WIDTH = 210;
    private static final int HEIGHT = 142;
    private static final int ROW_HEIGHT = 11;

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

        graphics.fill(X, Y, X + WIDTH, Y + HEIGHT, COLOR_BG);
        graphics.outline(X, Y, WIDTH, HEIGHT, COLOR_BORDER);
        graphics.fill(X, Y, X + WIDTH, Y + 2, COLOR_HEADER);

        int textX = X + 6;
        int textY = Y + 6;

        String profileName = config.getActiveProfile().toUpperCase();
        graphics.text(font, "Lilt", textX, textY, COLOR_HEADER);
        graphics.text(font, "[" + profileName + "]", textX + WIDTH - font.width("[" + profileName + "]") - 12, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT + 2;

        double fps = monitor.getCurrentFps();
        double avgFps = monitor.getAverageFps();
        double low1Pct = monitor.getOnePercentLowFps();
        int fpsColor = fps >= 55 ? COLOR_GOOD : fps >= 30 ? COLOR_WARN : COLOR_ALERT;
        graphics.text(font, String.format("FPS: %.0f  |  Avg: %.0f  |  1%%: %.0f", fps, avgFps, low1Pct), textX, textY, fpsColor);
        textY += ROW_HEIGHT;

        double frameMs = monitor.getFrameTimeMs();
        double p50 = monitor.getP50FrameTimeMs();
        double p99 = monitor.getP99FrameTimeMs();
        graphics.text(font, String.format("Frame: %.1fms (P50: %.1f, P99: %.1f)", frameMs, p50, p99), textX, textY, COLOR_TEXT_PRIMARY);
        textY += ROW_HEIGHT;

        double clientTickMs = monitor.getLastTickDurationMs();
        int tickColor = clientTickMs < 50 ? COLOR_GOOD : COLOR_ALERT;
        if (monitor.isIntegratedServerRunning()) {
            graphics.text(font, String.format("Tick: C %.1fms  |  S %.1fms", clientTickMs, monitor.getIntegratedServerTickMs()), textX, textY, tickColor);
        } else {
            graphics.text(font, String.format("Tick: C %.1fms (Remote Server)", clientTickMs), textX, textY, tickColor);
        }
        textY += ROW_HEIGHT;

        long usedMb = hardwareInfo.getUsedHeapMb();
        long maxMb = hardwareInfo.getMaxHeapMb();
        double memRatio = maxMb > 0 ? (double) usedMb / maxMb : 0;
        int memColor = memRatio < 0.75 ? COLOR_GOOD : memRatio < 0.88 ? COLOR_WARN : COLOR_ALERT;
        graphics.text(font, String.format("Memory: %d / %d MB (%.0f%%)", usedMb, maxMb, memRatio * 100), textX, textY, memColor);
        textY += ROW_HEIGHT;

        graphics.text(font, String.format("GC: %d pauses (+%dms)", monitor.getGcCountDelta(), monitor.getGcTimeDeltaMs()), textX, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT;

        String renderer = hardwareInfo.getGpuRenderer();
        if (renderer.length() > 28) renderer = renderer.substring(0, 25) + "...";
        graphics.text(font, "GPU: " + renderer, textX, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT;

        graphics.text(font, "API: " + hardwareInfo.getGraphicsBackend(), textX, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT;

        PerformanceDiagnostics.PressureType pressure = diagnostics.getCurrentPressure();
        int pressureColor = switch (pressure) {
            case NONE -> COLOR_GOOD;
            case UNKNOWN -> COLOR_TEXT_MUTED;
            default -> COLOR_WARN;
        };
        graphics.text(font, "State: " + pressure.getDisplayName(), textX, textY, pressureColor);
        textY += ROW_HEIGHT;

        String adaptiveLabel = config.isAdaptiveEnabled() ? "Adaptive: Active" : "Adaptive: Off";
        int adaptiveColor = config.isAdaptiveEnabled() ? COLOR_GOOD : COLOR_TEXT_MUTED;
        graphics.text(font, adaptiveLabel, textX, textY, adaptiveColor);
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
