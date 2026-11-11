package dev.lilt.diagnostics;

import dev.lilt.monitor.PerformanceMonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Classifies performance pressure based on real measurements.
 * Only reports a classification when sufficient data supports it.
 */
public class PerformanceDiagnostics {

    private final PerformanceMonitor monitor;

    private PressureType currentPressure = PressureType.UNKNOWN;
    private String pressureDescription = "Collecting data...";
    private int stableTickCount = 0;

    // Minimum frames before making a classification
    private static final int MIN_FRAMES_FOR_CLASSIFICATION = 120;
    // Thresholds
    private static final double HIGH_FRAME_VARIANCE_THRESHOLD = 3.0; // p99/p50 ratio
    private static final double MEMORY_PRESSURE_THRESHOLD = 0.85; // heap usage ratio
    private static final double TICK_OVERRUN_MS = 50.0; // ms per tick (normal is 50ms for 20 TPS)
    private static final double SEVERE_FRAME_TIME_MS = 33.3; // ~30 FPS

    public PerformanceDiagnostics(PerformanceMonitor monitor) {
        this.monitor = monitor;
    }

    public void update() {
        stableTickCount++;

        if (monitor.getFrameCount() < MIN_FRAMES_FOR_CLASSIFICATION) {
            currentPressure = PressureType.UNKNOWN;
            pressureDescription = "Collecting data (" + monitor.getFrameCount() + "/" + MIN_FRAMES_FOR_CLASSIFICATION + ")";
            return;
        }

        // Check memory pressure first
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long used = memBean.getHeapMemoryUsage().getUsed();
        long max = memBean.getHeapMemoryUsage().getMax();
        double heapRatio = max > 0 ? (double) used / max : 0;
        boolean memoryPressure = heapRatio > MEMORY_PRESSURE_THRESHOLD;
        boolean frequentGc = monitor.getGcTimeDeltaMs() > 10;

        // Check frame time issues
        double p50 = monitor.getP50FrameTimeMs();
        double p99 = monitor.getP99FrameTimeMs();
        double varianceRatio = p50 > 0 ? p99 / p50 : 1.0;
        boolean highVariance = varianceRatio > HIGH_FRAME_VARIANCE_THRESHOLD;
        boolean severeFrameTime = p50 > SEVERE_FRAME_TIME_MS;

        // Check tick overrun (potential CPU bottleneck)
        boolean tickOverrun = monitor.getLastTickDurationMs() > TICK_OVERRUN_MS;

        // Classify
        if (memoryPressure && frequentGc) {
            currentPressure = PressureType.MEMORY;
            pressureDescription = String.format("High memory usage (%.0f%%), frequent GC", heapRatio * 100);
        } else if (tickOverrun && severeFrameTime) {
            currentPressure = PressureType.CPU_BOUND;
            pressureDescription = String.format("Tick overrun (%.1fms), high frame times", monitor.getLastTickDurationMs());
        } else if (severeFrameTime && !tickOverrun) {
            // High frame times but ticks are fine - likely GPU
            currentPressure = PressureType.GPU_BOUND;
            pressureDescription = String.format("High frame times (P50=%.1fms) with normal tick rate", p50);
        } else if (highVariance) {
            currentPressure = PressureType.MIXED;
            pressureDescription = String.format("High frame time variance (P99/P50=%.1fx)", varianceRatio);
        } else {
            currentPressure = PressureType.NONE;
            pressureDescription = "No significant pressure detected";
        }
    }

    public PressureType getCurrentPressure() { return currentPressure; }
    public String getPressureDescription() { return pressureDescription; }

    public enum PressureType {
        NONE("None"),
        CPU_BOUND("CPU-bound"),
        GPU_BOUND("GPU-bound"),
        MEMORY("Memory pressure"),
        CHUNK_PROCESSING("Chunk processing"),
        MIXED("Mixed"),
        UNKNOWN("Unknown");

        private final String displayName;
        PressureType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
