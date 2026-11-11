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
        // Less than 120 frames
        for (int i = 0; i < 50; i++) {
            monitor.recordFrame(16_000_000L);
        }
        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.UNKNOWN, diagnostics.getCurrentPressure());
        assertTrue(diagnostics.getPressureDescription().contains("Collecting data"));
    }

    @Test
    void testNoneUnderGoodPerformance() {
        // 150 frames of solid 60 FPS (16.6ms)
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(16_666_666L);
        }
        monitor.recordTick(10_000_000L); // 10ms tick

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.NONE, diagnostics.getCurrentPressure());
    }

    @Test
    void testGpuBoundClassification() {
        // 150 slow frames (50ms = 20 FPS) with fast ticks (5ms)
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(50_000_000L);
        }
        monitor.recordTick(5_000_000L); // fast server tick

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.GPU_BOUND, diagnostics.getCurrentPressure());
    }

    @Test
    void testCpuBoundClassification() {
        // 150 slow frames (50ms) with overloaded tick (>50ms)
        for (int i = 0; i < 150; i++) {
            monitor.recordFrame(50_000_000L);
        }
        monitor.recordTick(65_000_000L); // tick overrun

        diagnostics.update();
        assertEquals(PerformanceDiagnostics.PressureType.CPU_BOUND, diagnostics.getCurrentPressure());
    }
}
