package dev.lilt.diagnostics;

import dev.lilt.monitor.PerformanceMonitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class PerformanceDiagnostics {

    private final PerformanceMonitor monitor;

    private PressureType currentPressure = PressureType.UNKNOWN;
    private String pressureDescription = "Collecting data...";

    private static final int MIN_FRAMES_FOR_CLASSIFICATION = 120;
    private static final double HIGH_FRAME_VARIANCE_THRESHOLD = 3.0;
    private static final double MEMORY_PRESSURE_THRESHOLD = 0.85;
    private static final double TICK_OVERRUN_MS = 50.0;
    private static final double SEVERE_FRAME_TIME_MS = 33.3;

    private double measuredGpuTimeMs = -1.0;
    private boolean gpuTimingSupported = false;

    public PerformanceDiagnostics(PerformanceMonitor monitor) {
        this.monitor = monitor;
    }

    public void setGpuTimingMeasurement(double gpuTimeMs) {
        this.measuredGpuTimeMs = gpuTimeMs;
        this.gpuTimingSupported = (gpuTimeMs >= 0.0);
    }

    public void update() {
        if (monitor.getFrameCount() < MIN_FRAMES_FOR_CLASSIFICATION) {
            currentPressure = PressureType.UNKNOWN;
            pressureDescription = "Collecting data (" + monitor.getFrameCount() + "/" + MIN_FRAMES_FOR_CLASSIFICATION + ")";
            return;
        }

        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        long used = memBean.getHeapMemoryUsage().getUsed();
        long max = memBean.getHeapMemoryUsage().getMax();
        double heapRatio = max > 0 ? (double) used / max : 0;
        boolean memoryPressure = heapRatio > MEMORY_PRESSURE_THRESHOLD;
        boolean frequentGc = monitor.getGcTimeDeltaMs() > 10;

        double p50 = monitor.getP50FrameTimeMs();
        double p99 = monitor.getP99FrameTimeMs();
        double varianceRatio = p50 > 0 ? p99 / p50 : 1.0;
        boolean highVariance = varianceRatio > HIGH_FRAME_VARIANCE_THRESHOLD;
        boolean severeFrameTime = p50 > SEVERE_FRAME_TIME_MS;

        double clientTickMs = monitor.getLastTickDurationMs();
        boolean clientTickOverrun = clientTickMs > TICK_OVERRUN_MS;
        boolean serverTickOverrun = false;
        double serverTickMs = 0;

        if (monitor.isIntegratedServerRunning()) {
            serverTickMs = monitor.getIntegratedServerTickMs();
            serverTickOverrun = serverTickMs > TICK_OVERRUN_MS;
        }

        if (memoryPressure && frequentGc) {
            currentPressure = PressureType.MEMORY;
            pressureDescription = String.format("High heap usage (%.0f%%), frequent GC pauses", heapRatio * 100);
        } else if (clientTickOverrun || serverTickOverrun) {
            currentPressure = PressureType.CPU_BOUND;
            if (serverTickOverrun) {
                pressureDescription = String.format("Integrated server tick overrun (%.1fms)", serverTickMs);
            } else {
                pressureDescription = String.format("Client tick overrun (%.1fms)", clientTickMs);
            }
        } else if (severeFrameTime) {
            if (gpuTimingSupported && measuredGpuTimeMs > 0) {
                double gpuRatio = measuredGpuTimeMs / p50;
                if (gpuRatio > 0.80) {
                    currentPressure = PressureType.GPU_BOUND;
                    pressureDescription = String.format("GPU bound (GPU time=%.1fms / P50=%.1fms)", measuredGpuTimeMs, p50);
                } else {
                    currentPressure = PressureType.UNKNOWN;
                    pressureDescription = String.format("High frame interval (P50=%.1fms), GPU time=%.1fms", p50, measuredGpuTimeMs);
                }
            } else {
                currentPressure = PressureType.UNKNOWN;
                pressureDescription = String.format("Elevated frame intervals (P50=%.1fms); GPU query unavailable", p50);
            }
        } else if (highVariance) {
            currentPressure = PressureType.MIXED;
            pressureDescription = String.format("High frame-time variance (P99/P50=%.1fx)", varianceRatio);
        } else {
            currentPressure = PressureType.NONE;
            pressureDescription = "Stable performance; no bottleneck detected";
        }
    }

    public PressureType getCurrentPressure() { return currentPressure; }
    public String getPressureDescription() { return pressureDescription; }
    public boolean isGpuTimingSupported() { return gpuTimingSupported; }

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
