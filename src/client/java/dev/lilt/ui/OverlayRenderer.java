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

    private static final long CACHE_UPDATE_INTERVAL_MS = 250;
    private long lastCacheUpdateMs = 0;

    private String cachedProfileTag = "";
    private String cachedFpsLine = "";
    private int cachedFpsColor = COLOR_GOOD;
    private String cachedFrameLine = "";
    private String cachedTickLine = "";
    private int cachedTickColor = COLOR_GOOD;
    private String cachedMemoryLine = "";
    private int cachedMemoryColor = COLOR_GOOD;
    private String cachedGcLine = "";
    private String cachedGpuLine = "";
    private String cachedBackendLine = "";
    private String cachedStateLine = "";
    private int cachedStateColor = COLOR_GOOD;
    private String cachedAdaptiveLine = "";
    private int cachedAdaptiveColor = COLOR_GOOD;

    public OverlayRenderer(LiltConfig config, PerformanceMonitor monitor,
                           PerformanceDiagnostics diagnostics, HardwareInfo hardwareInfo) {
        this.config = config;
        this.monitor = monitor;
        this.diagnostics = diagnostics;
        this.hardwareInfo = hardwareInfo;
        this.visible = config.isOverlayEnabled();
    }

    private void refreshCache(long nowMs) {
        lastCacheUpdateMs = nowMs;

        cachedProfileTag = "[" + config.getActiveProfile().toUpperCase() + "]";

        double fps = monitor.getCurrentFps();
        double avgFps = monitor.getAverageFps();
        double low1Pct = monitor.getOnePercentLowFps();
        cachedFpsColor = fps >= 55 ? COLOR_GOOD : fps >= 30 ? COLOR_WARN : COLOR_ALERT;
        cachedFpsLine = String.format("FPS: %.0f  |  Avg: %.0f  |  1%%: %.0f", fps, avgFps, low1Pct);

        double frameMs = monitor.getFrameTimeMs();
        double p50 = monitor.getP50FrameTimeMs();
        double p99 = monitor.getP99FrameTimeMs();
        cachedFrameLine = String.format("Frame: %.1fms (P50: %.1f, P99: %.1f)", frameMs, p50, p99);

        double clientTickMs = monitor.getClientTickDurationMs();
        cachedTickColor = clientTickMs < 45.0 ? COLOR_GOOD : COLOR_ALERT;
        if (monitor.isIntegratedServerRunning()) {
            cachedTickLine = String.format("Tick: C %.1fms  |  S %.1fms", clientTickMs, monitor.getIntegratedServerTickMs());
        } else {
            cachedTickLine = String.format("Tick: C %.1fms (Remote Server)", clientTickMs);
        }

        long usedMb = hardwareInfo.getUsedHeapMb();
        long maxMb = hardwareInfo.getMaxHeapMb();
        double memRatio = maxMb > 0 ? (double) usedMb / maxMb : 0;
        cachedMemoryColor = memRatio < 0.75 ? COLOR_GOOD : memRatio < 0.88 ? COLOR_WARN : COLOR_ALERT;
        cachedMemoryLine = String.format("Memory: %d / %d MB (%.0f%%)", usedMb, maxMb, memRatio * 100);

        cachedGcLine = String.format("GC: %d pauses (+%dms)", monitor.getGcCountDelta(), monitor.getGcTimeDeltaMs());

        String renderer = hardwareInfo.getGpuRenderer();
        if (renderer == null || renderer.isBlank()) renderer = "unavailable";
        if (renderer.length() > 28) renderer = renderer.substring(0, 25) + "...";
        cachedGpuLine = "GPU: " + renderer;

        String backend = hardwareInfo.getGraphicsBackend();
        if (backend == null || backend.isBlank()) backend = "unavailable";
        cachedBackendLine = "API: " + backend;

        PerformanceDiagnostics.PressureType pressure = diagnostics.getCurrentPressure();
        cachedStateColor = switch (pressure) {
            case NONE -> COLOR_GOOD;
            case UNKNOWN -> COLOR_TEXT_MUTED;
            default -> COLOR_WARN;
        };
        cachedStateLine = "State: " + pressure.getDisplayName();

        cachedAdaptiveLine = config.isAdaptiveEnabled() ? "Adaptive: Active" : "Adaptive: Off";
        cachedAdaptiveColor = config.isAdaptiveEnabled() ? COLOR_GOOD : COLOR_TEXT_MUTED;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!visible) return;

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        if (font == null) return;

        long now = System.currentTimeMillis();
        if (now - lastCacheUpdateMs >= CACHE_UPDATE_INTERVAL_MS || lastCacheUpdateMs == 0) {
            refreshCache(now);
        }

        graphics.fill(X, Y, X + WIDTH, Y + HEIGHT, COLOR_BG);
        graphics.outline(X, Y, WIDTH, HEIGHT, COLOR_BORDER);
        graphics.fill(X, Y, X + WIDTH, Y + 2, COLOR_HEADER);

        int textX = X + 6;
        int textY = Y + 6;

        graphics.text(font, "Lilt", textX, textY, COLOR_HEADER);
        graphics.text(font, cachedProfileTag, textX + WIDTH - font.width(cachedProfileTag) - 12, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT + 2;

        graphics.text(font, cachedFpsLine, textX, textY, cachedFpsColor);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedFrameLine, textX, textY, COLOR_TEXT_PRIMARY);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedTickLine, textX, textY, cachedTickColor);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedMemoryLine, textX, textY, cachedMemoryColor);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedGcLine, textX, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedGpuLine, textX, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedBackendLine, textX, textY, COLOR_TEXT_MUTED);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedStateLine, textX, textY, cachedStateColor);
        textY += ROW_HEIGHT;

        graphics.text(font, cachedAdaptiveLine, textX, textY, cachedAdaptiveColor);
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
