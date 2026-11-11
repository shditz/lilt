package dev.lilt.monitor;

import dev.lilt.config.LiltConfig;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

public class PerformanceMonitor {

    private final LiltConfig config;
    private final long[] frameTimes;
    private final long[] sortBuffer;
    private int frameIndex = 0;
    private int frameCount = 0;
    private volatile boolean metricsDirty = true;

    private long lastTickStartNanos = 0;
    private long lastClientTickDurationNanos = 0;
    private long integratedServerTickNanos = 0;
    private boolean integratedServerRunning = false;
    private long lastFrameNanos = 0;

    private long lastGcCount = 0;
    private long lastGcTimeMs = 0;
    private long gcCountDelta = 0;
    private long gcTimeDeltaMs = 0;

    private volatile double currentFps = 0;
    private volatile double averageFps = 0;
    private volatile double frameIntervalMs = 0;
    private volatile double p50FrameTimeMs = 0;
    private volatile double p95FrameTimeMs = 0;
    private volatile double p99FrameTimeMs = 0;
    private volatile double onePercentLowFps = 0;

    public PerformanceMonitor() {
        this(new LiltConfig());
    }

    public PerformanceMonitor(LiltConfig config) {
        this.config = config;
        int size = Math.max(60, config.getMonitorWindowSize());
        this.frameTimes = new long[size];
        this.sortBuffer = new long[size];
        this.lastFrameNanos = System.nanoTime();
    }

    public synchronized void recordFrame(long elapsedNanos) {
        if (elapsedNanos > 0 && elapsedNanos < 1_000_000_000L) {
            frameTimes[frameIndex] = elapsedNanos;
            frameIndex = (frameIndex + 1) % frameTimes.length;
            if (frameCount < frameTimes.length) {
                frameCount++;
            }
            frameIntervalMs = elapsedNanos / 1_000_000.0;
            currentFps = 1_000_000_000.0 / elapsedNanos;
            metricsDirty = true;
        }
    }

    public void recordTick(long durationNanos) {
        this.lastClientTickDurationNanos = durationNanos;
    }

    public void recordIntegratedServerTick(long durationNanos) {
        this.integratedServerTickNanos = durationNanos;
        this.integratedServerRunning = true;
    }

    public void setRemoteServer() {
        this.integratedServerRunning = false;
        this.integratedServerTickNanos = 0;
    }

    public synchronized void tick() {
        long now = System.nanoTime();

        long elapsed = now - lastFrameNanos;
        lastFrameNanos = now;
        if (elapsed > 0 && elapsed < 1_000_000_000L && frameCount == 0) {
            frameTimes[frameIndex] = elapsed;
            frameIndex = (frameIndex + 1) % frameTimes.length;
            if (frameCount < frameTimes.length) {
                frameCount++;
            }
            frameIntervalMs = elapsed / 1_000_000.0;
            currentFps = 1_000_000_000.0 / elapsed;
            metricsDirty = true;
        }

        if (lastTickStartNanos > 0) {
            lastClientTickDurationNanos = now - lastTickStartNanos;
        }
        lastTickStartNanos = now;

        updateGcMetrics();
        ensureMetricsComputed();
    }

    private void updateGcMetrics() {
        long totalCount = 0;
        long totalTimeMs = 0;
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            long count = gc.getCollectionCount();
            long time = gc.getCollectionTime();
            if (count >= 0) totalCount += count;
            if (time >= 0) totalTimeMs += time;
        }
        gcCountDelta = totalCount - lastGcCount;
        gcTimeDeltaMs = totalTimeMs - lastGcTimeMs;
        lastGcCount = totalCount;
        lastGcTimeMs = totalTimeMs;
    }

    public synchronized void ensureMetricsComputed() {
        if (!metricsDirty || frameCount == 0) return;

        System.arraycopy(frameTimes, 0, sortBuffer, 0, frameCount);
        Arrays.sort(sortBuffer, 0, frameCount);

        long sum = 0;
        for (int i = 0; i < frameCount; i++) {
            sum += sortBuffer[i];
        }
        double avgNs = (double) sum / frameCount;
        if (avgNs > 0) {
            averageFps = 1_000_000_000.0 / avgNs;
        }

        p50FrameTimeMs = percentile(sortBuffer, frameCount, 0.50) / 1_000_000.0;
        p95FrameTimeMs = percentile(sortBuffer, frameCount, 0.95) / 1_000_000.0;
        p99FrameTimeMs = percentile(sortBuffer, frameCount, 0.99) / 1_000_000.0;

        int onePercentCount = Math.max(1, frameCount / 100);
        long worstSum = 0;
        for (int i = frameCount - onePercentCount; i < frameCount; i++) {
            worstSum += sortBuffer[i];
        }
        double avgWorstNs = (double) worstSum / onePercentCount;
        if (avgWorstNs > 0) {
            onePercentLowFps = 1_000_000_000.0 / avgWorstNs;
        }

        metricsDirty = false;
    }

    private long percentile(long[] sorted, int count, double p) {
        if (count == 0) return 0;
        int index = (int) Math.ceil(p * count) - 1;
        return sorted[Math.max(0, Math.min(index, count - 1))];
    }

    public LiltConfig getConfig() { return config; }
    public double getCurrentFps() { return currentFps; }
    public double getAverageFps() { ensureMetricsComputed(); return averageFps; }
    public double getFrameTimeMs() { return frameIntervalMs; }
    public double getAvgFrameTimeMs() { ensureMetricsComputed(); return averageFps > 0 ? 1000.0 / averageFps : 0.0; }
    public double getP50FrameTimeMs() { ensureMetricsComputed(); return p50FrameTimeMs; }
    public double getP95FrameTimeMs() { ensureMetricsComputed(); return p95FrameTimeMs; }
    public double getP99FrameTimeMs() { ensureMetricsComputed(); return p99FrameTimeMs; }
    public double getOnePercentLowFps() { ensureMetricsComputed(); return onePercentLowFps; }
    public long getGcCountDelta() { return gcCountDelta; }
    public long getGcTimeDeltaMs() { return gcTimeDeltaMs; }
    public long getLastTickDurationNanos() { return lastClientTickDurationNanos; }
    public double getLastTickDurationMs() { return lastClientTickDurationNanos / 1_000_000.0; }
    public double getLastTickTimeMs() { return getLastTickDurationMs(); }
    public double getIntegratedServerTickMs() { return integratedServerTickNanos / 1_000_000.0; }
    public boolean isIntegratedServerRunning() { return integratedServerRunning; }

    public double getFpsRatio(int targetFps) {
        ensureMetricsComputed();
        if (targetFps <= 0 || averageFps <= 0) return 1.0;
        return averageFps / targetFps;
    }

    public int getFrameCount() { return frameCount; }
}
