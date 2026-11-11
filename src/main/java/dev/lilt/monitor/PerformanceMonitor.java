package dev.lilt.monitor;

import dev.lilt.config.LiltConfig;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

/**
 * Lightweight performance monitoring.
 * Collects frame times, FPS, GC activity, and tick timing.
 * Avoids expensive instrumentation.
 */
public class PerformanceMonitor {

    private final LiltConfig config;

    // Frame timing ring buffer
    private final long[] frameTimes; // nanoseconds
    private int frameIndex = 0;
    private int frameCount = 0;

    // Tick timing
    private long lastTickStartNanos = 0;
    private long lastTickDurationNanos = 0;
    private long lastFrameNanos = 0;

    // GC tracking
    private long lastGcCount = 0;
    private long lastGcTimeMs = 0;
    private long gcCountDelta = 0;
    private long gcTimeDeltaMs = 0;

    // Current computed metrics
    private double currentFps = 0;
    private double averageFps = 0;
    private double frameTimeMs = 0;
    private double p50FrameTimeMs = 0;
    private double p95FrameTimeMs = 0;
    private double p99FrameTimeMs = 0;
    private double onePercentLowFps = 0;

    public PerformanceMonitor() {
        this(new LiltConfig());
    }

    public PerformanceMonitor(LiltConfig config) {
        this.config = config;
        this.frameTimes = new long[config.getMonitorWindowSize()];
        this.lastFrameNanos = System.nanoTime();
    }

    /**
     * Records an explicitly measured frame duration.
     * Safe to call from the render thread on each frame boundary.
     */
    public synchronized void recordFrame(long elapsedNanos) {
        if (elapsedNanos > 0 && elapsedNanos < 1_000_000_000L) {
            frameTimes[frameIndex] = elapsedNanos;
            frameIndex = (frameIndex + 1) % frameTimes.length;
            if (frameCount < frameTimes.length) {
                frameCount++;
            }
            computeMetrics();
        }
    }

    /**
     * Records an explicitly measured tick duration.
     */
    public void recordTick(long durationNanos) {
        this.lastTickDurationNanos = durationNanos;
    }

    /**
     * Called once per client tick to maintain tick and GC metrics.
     */
    public synchronized void tick() {
        long now = System.nanoTime();

        // Record frame time if not driven by render loop
        long elapsed = now - lastFrameNanos;
        lastFrameNanos = now;

        if (elapsed > 0 && elapsed < 1_000_000_000L) {
            frameTimes[frameIndex] = elapsed;
            frameIndex = (frameIndex + 1) % frameTimes.length;
            if (frameCount < frameTimes.length) {
                frameCount++;
            }
        }

        // Tick timing
        if (lastTickStartNanos > 0) {
            lastTickDurationNanos = now - lastTickStartNanos;
        }
        lastTickStartNanos = now;

        // GC tracking
        updateGcMetrics();

        // Compute derived metrics
        computeMetrics();
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

    private void computeMetrics() {
        if (frameCount == 0) return;

        // Current FPS from last frame time
        long lastFrame = frameTimes[(frameIndex - 1 + frameTimes.length) % frameTimes.length];
        if (lastFrame > 0) {
            currentFps = 1_000_000_000.0 / lastFrame;
            frameTimeMs = lastFrame / 1_000_000.0;
        }

        // Compute percentiles from the window
        long[] sorted = new long[frameCount];
        if (frameCount == frameTimes.length) {
            System.arraycopy(frameTimes, 0, sorted, 0, frameCount);
        } else {
            System.arraycopy(frameTimes, 0, sorted, 0, frameCount);
        }
        Arrays.sort(sorted);

        // Average FPS
        long sum = 0;
        for (int i = 0; i < frameCount; i++) {
            sum += sorted[i];
        }
        double avgFrameNs = (double) sum / frameCount;
        if (avgFrameNs > 0) {
            averageFps = 1_000_000_000.0 / avgFrameNs;
        }

        // Percentiles
        p50FrameTimeMs = percentile(sorted, 0.50) / 1_000_000.0;
        p95FrameTimeMs = percentile(sorted, 0.95) / 1_000_000.0;
        p99FrameTimeMs = percentile(sorted, 0.99) / 1_000_000.0;

        // 1% low FPS: average of the worst 1% of frames
        int onePercentCount = Math.max(1, frameCount / 100);
        long worstSum = 0;
        for (int i = frameCount - onePercentCount; i < frameCount; i++) {
            worstSum += sorted[i];
        }
        double avgWorst = (double) worstSum / onePercentCount;
        if (avgWorst > 0) {
            onePercentLowFps = 1_000_000_000.0 / avgWorst;
        }
    }

    private long percentile(long[] sorted, double p) {
        if (sorted.length == 0) return 0;
        int index = (int) Math.ceil(p * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(index, sorted.length - 1))];
    }

    // Getters
    public LiltConfig getConfig() { return config; }
    public double getCurrentFps() { return currentFps; }
    public double getAverageFps() { return averageFps; }
    public double getFrameTimeMs() { return frameTimeMs; }
    public double getAvgFrameTimeMs() { return averageFps > 0 ? 1000.0 / averageFps : 0.0; }
    public double getP50FrameTimeMs() { return p50FrameTimeMs; }
    public double getP95FrameTimeMs() { return p95FrameTimeMs; }
    public double getP99FrameTimeMs() { return p99FrameTimeMs; }
    public double getOnePercentLowFps() { return onePercentLowFps; }
    public long getGcCountDelta() { return gcCountDelta; }
    public long getGcTimeDeltaMs() { return gcTimeDeltaMs; }
    public long getLastTickDurationNanos() { return lastTickDurationNanos; }
    public double getLastTickDurationMs() { return lastTickDurationNanos / 1_000_000.0; }
    public double getLastTickTimeMs() { return getLastTickDurationMs(); }

    /**
     * Returns the ratio of current FPS to target FPS.
     * Values below 1.0 indicate the game is under target.
     */
    public double getFpsRatio(int targetFps) {
        if (targetFps <= 0 || averageFps <= 0) return 1.0;
        return averageFps / targetFps;
    }

    public int getFrameCount() { return frameCount; }
}
