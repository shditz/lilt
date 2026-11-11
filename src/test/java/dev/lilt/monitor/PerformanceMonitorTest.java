package dev.lilt.monitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceMonitorTest {

    private PerformanceMonitor monitor;

    @BeforeEach
    void setUp() {
        monitor = new PerformanceMonitor();
    }

    @Test
    void testInitialState() {
        assertEquals(0, monitor.getCurrentFps());
        assertEquals(0.0, monitor.getAvgFrameTimeMs());
        assertEquals(0.0, monitor.getP50FrameTimeMs());
        assertEquals(0.0, monitor.getP95FrameTimeMs());
        assertEquals(0.0, monitor.getP99FrameTimeMs());
        assertEquals(0.0, monitor.getOnePercentLowFps());
    }

    @Test
    void testFrameRecordingAndPercentiles() {
        // Record 100 frames with varying durations: 10ms (most) and a few slow 50ms frames
        for (int i = 0; i < 90; i++) {
            monitor.recordFrame(10_000_000L); // 10ms
        }
        for (int i = 0; i < 10; i++) {
            monitor.recordFrame(50_000_000L); // 50ms
        }

        assertTrue(monitor.getAvgFrameTimeMs() >= 10.0 && monitor.getAvgFrameTimeMs() <= 20.0);
        assertEquals(10.0, monitor.getP50FrameTimeMs(), 0.5);
        assertTrue(monitor.getP95FrameTimeMs() >= 10.0);
        assertTrue(monitor.getP99FrameTimeMs() >= 40.0);
        assertTrue(monitor.getOnePercentLowFps() > 0.0);
    }

    @Test
    void testTickTiming() {
        monitor.recordTick(5_000_000L); // 5ms
        assertEquals(5.0, monitor.getLastTickTimeMs(), 0.01);
    }
}

