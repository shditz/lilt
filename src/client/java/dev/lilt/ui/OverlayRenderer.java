package dev.lilt.ui;

import dev.lilt.config.LiltConfig;
import dev.lilt.diagnostics.PerformanceDiagnostics;
import dev.lilt.hardware.HardwareInfo;
import dev.lilt.monitor.PerformanceMonitor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class OverlayRenderer {

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

    private void refreshCache(long now) {
        lastCacheUpdateMs = now;

        cachedProfileTag = "[" + config.getActiveProfile().toUpperCase() + "]";

        int fps = (int) Math.round(monitor.getCurrentFps());
        double p1Low = monitor.getOnePercentLowFps();
        cachedFpsLine = String.format("FPS: %d  (1%% low: %.1f)", fps, p1Low);
        if (fps >= 60) {
            cachedFpsColor = COLOR_GOOD;
        } else if (fps >= 30) {
            cachedFpsColor = COLOR_WARN;
        } else {
            cachedFpsColor = COLOR_ALERT;
        }

        double p50 = monitor.getP50FrameTimeMs();
        double p95 = monitor.getP95FrameTimeMs();
        double p99 = monitor.getP99FrameTimeMs();
        cachedFrameLine = String.format("Frame: p50 %.1f  p95 %.1f  p99 %.1f ms", p50, p95, p99);

        double tickMs = monitor.getLastTickTimeMs();
        cachedTickLine = String.format("Tick: %.1f ms (50.0 cap)", tickMs);
        if (tickMs <= 35.0) {
            cachedTickColor = COLOR_GOOD;
        } else if (tickMs <= 50.0) {
            cachedTickColor = COLOR_WARN;
        } else {
            cachedTickColor = COLOR_ALERT;
        }

        long usedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024L * 1024L);
        long maxMb = hardwareInfo.getMaxHeapMb();
        double heapPct = maxMb > 0 ? (double) usedMb / maxMb * 100.0 : 0.0;
        cachedMemoryLine = String.format("Heap: %d / %d MB (%.0f%%)", usedMb, maxMb, heapPct);
        if (heapPct < 75.0) {
            cachedMemoryColor = COLOR_GOOD;
        } else if (heapPct < 88.0) {
            cachedMemoryColor = COLOR_WARN;
        } else {
            cachedMemoryColor = COLOR_ALERT;
        }

        long gcCount = monitor.getGcCountDelta();
        long gcTimeMs = monitor.getGcTimeDeltaMs();
        cachedGcLine = String.format("GC: %d pauses (%d ms total)", gcCount, gcTimeMs);

        String renderer = hardwareInfo.getGpuRenderer();
        if (renderer == null || renderer.isBlank()) renderer = "detecting...";
        if (renderer.length() > 28) renderer = renderer.substring(0, 26) + "..";
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

    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!visible) return;

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;
        if (font == null) return;

        long now = System.currentTimeMillis();
        if (now - lastCacheUpdateMs >= CACHE_UPDATE_INTERVAL_MS || lastCacheUpdateMs == 0) {
            refreshCache(now);
        }

        graphics.fill(X, Y, X + WIDTH, Y + HEIGHT, COLOR_BG);
        graphics.renderOutline(X, Y, WIDTH, HEIGHT, COLOR_BORDER);
        graphics.fill(X, Y, X + WIDTH, Y + 2, COLOR_HEADER);

        int textX = X + 6;
        int textY = Y + 6;

        graphics.drawString(font, "Lilt", textX, textY, COLOR_HEADER, false);
        graphics.drawString(font, cachedProfileTag, textX + WIDTH - font.width(cachedProfileTag) - 12, textY, COLOR_TEXT_MUTED, false);
        textY += ROW_HEIGHT + 2;

        graphics.drawString(font, cachedFpsLine, textX, textY, cachedFpsColor, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedFrameLine, textX, textY, COLOR_TEXT_PRIMARY, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedTickLine, textX, textY, cachedTickColor, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedMemoryLine, textX, textY, cachedMemoryColor, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedGcLine, textX, textY, COLOR_TEXT_MUTED, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedGpuLine, textX, textY, COLOR_TEXT_MUTED, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedBackendLine, textX, textY, COLOR_TEXT_MUTED, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedStateLine, textX, textY, cachedStateColor, false);
        textY += ROW_HEIGHT;

        graphics.drawString(font, cachedAdaptiveLine, textX, textY, cachedAdaptiveColor, false);
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
