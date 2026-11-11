package dev.lilt.adaptive;

import dev.lilt.compat.ModCompat;
import dev.lilt.config.LiltConfig;
import dev.lilt.diagnostics.PerformanceDiagnostics;
import dev.lilt.monitor.PerformanceMonitor;
import dev.lilt.optimization.particle.ParticleOptimizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveControllerTest {

    private LiltConfig config;
    private PerformanceMonitor monitor;
    private PerformanceDiagnostics diagnostics;
    private ParticleOptimizer particleOptimizer;
    private AdaptiveController controller;

    @BeforeEach
    void setUp() {
        config = new LiltConfig();
        monitor = new PerformanceMonitor();
        diagnostics = new PerformanceDiagnostics(monitor);
        particleOptimizer = new ParticleOptimizer(config, new ModCompat());
        controller = new AdaptiveController(config, monitor, diagnostics, particleOptimizer);
    }

    @Test
    void testInitialState() {
        assertEquals(AdaptiveController.AdaptiveState.STABLE, controller.getState());
        assertEquals("none", controller.getLastAction());
    }

    @Test
    void testDoesNotActWithoutSufficientFrames() {
        // Only 10 frames recorded (< 60 threshold)
        for (int i = 0; i < 10; i++) {
            monitor.recordFrame(50_000_000L); // 20 FPS (below target 60)
        }
        diagnostics.update();
        controller.tick();
        assertEquals(AdaptiveController.AdaptiveState.STABLE, controller.getState());
        assertEquals(1000, particleOptimizer.getEffectiveBudget());
    }

    @Test
    void testReducesWorkloadUnderPerformancePressure() {
        // Record 100 slow frames: 50ms = 20 FPS (< target 60 * 0.85 = 51)
        for (int i = 0; i < 100; i++) {
            monitor.recordFrame(50_000_000L);
        }

        controller.tick();

        assertEquals(AdaptiveController.AdaptiveState.REDUCING, controller.getState());
        // Initial budget was 1000, 20% reduction should make it 800
        assertEquals(800, particleOptimizer.getEffectiveBudget());
        assertTrue(controller.getLastAction().contains("Reduced particle budget"));
    }

    @Test
    void testRespectsCooldown() {
        // Record 100 slow frames
        for (int i = 0; i < 100; i++) {
            monitor.recordFrame(50_000_000L);
        }

        controller.tick();
        assertEquals(800, particleOptimizer.getEffectiveBudget());

        // Second tick immediately afterwards should be in cooldown, not reducing further
        controller.tick();
        assertEquals(800, particleOptimizer.getEffectiveBudget());
    }
}

