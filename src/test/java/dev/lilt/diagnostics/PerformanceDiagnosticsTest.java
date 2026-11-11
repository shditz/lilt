package dev.lilt.diagnostics;

import dev.lilt.monitor.PerformanceMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceDiagnosticsTest {

    private PerformanceMonitor monitor;
    private PerformanceDiagnostics diagnostics;

    @BeforeEach
    void setUp() {
        monitor = new PerformanceMonitor();
        diagnostics = new PerformanceDiagnostics(monitor);
    }

    @Test
    void testUnknownBeforeMinimumFrames() {
        for (int i = 0; i < 50; i++) {
            monitor.recordFrame(16_000_000L);
        }
        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.UNKNOWN, diagnostics.getCurrentPressure());
        assertTrue(diagnostics.getPressureDescription().contains("Collecting data"));
    }

    @Test
    void testNoneUnderGoodPerformance() {
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(16_666_666L);
        }
        monitor.recordTick(10_000_000L);

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.NONE, diagnostics.getCurrentPressure());
    }

    @Test
    void testHighFrameTimeWithoutGpuTimingReportsUnknown() {
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(50_000_000L);
        }
        monitor.recordTick(5_000_000L);

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.UNKNOWN, diagnostics.getCurrentPressure());
        assertTrue(diagnostics.getPressureDescription().contains("GPU query unavailable"));
    }

    @Test
    void testGpuBoundWithVerifiedTiming() {
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(50_000_000L);
        }
        monitor.recordTick(5_000_000L);
        diagnostics.setGpuTimingMeasurement(45.0);

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.GPU_BOUND, diagnostics.getCurrentPressure());
        assertTrue(diagnostics.getPressureDescription().contains("GPU bound"));
    }

    @Test
    void testCpuBoundClassification() {
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(50_000_000L);
        }
        monitor.recordTick(65_000_000L);

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.CPU_BOUND, diagnostics.getCurrentPressure());
    }
}
