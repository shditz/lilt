package dev.lilt.optimization.particle;

import dev.lilt.compat.ModCompat;
import dev.lilt.config.LiltConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticleOptimizerTest {

    private LiltConfig config;
    private ParticleOptimizer optimizer;

    @BeforeEach
    void setUp() {
        config = new LiltConfig();
        optimizer = new ParticleOptimizer(config, new ModCompat());
    }

    @Test
    void testAllowsParticleWithinBudgetAndDistance() {
        assertTrue(optimizer.shouldAllowParticle(100.0));
        assertEquals(1, optimizer.getCurrentParticleCount());
        assertEquals(0, optimizer.getRejectedThisTick());
    }

    @Test
    void testRejectsParticleExceedingDistance() {
        assertFalse(optimizer.shouldAllowParticle(1200.0));
        assertEquals(0, optimizer.getCurrentParticleCount());
        assertEquals(1, optimizer.getRejectedThisTick());
        assertEquals(1, optimizer.getTotalRejected());
    }

    @Test
    void testRejectsParticleExceedingBudget() {
        optimizer.setEffectiveBudget(50);
        optimizer.updateParticleCount(50);
        assertEquals(50, optimizer.getCurrentParticleCount());

        assertFalse(optimizer.shouldAllowParticle(50.0));
        assertEquals(1, optimizer.getRejectedThisTick());
        assertEquals(1, optimizer.getTotalRejected());
    }

    @Test
    void testNearPlayerParticlesAlwaysProtected() {
        optimizer.setEffectiveBudget(50);
        optimizer.updateParticleCount(50);

        assertTrue(optimizer.shouldAllowParticle(25.0));
        assertTrue(optimizer.shouldAllowParticle(4.0));
        assertTrue(optimizer.shouldAllowParticle(Double.NaN));
        assertEquals(0, optimizer.getRejectedThisTick());
    }

    @Test
    void testWorldUnloadResetsState() {
        optimizer.updateParticleCount(500);
        optimizer.shouldAllowParticle(2000.0);
        assertTrue(optimizer.getCurrentParticleCount() > 0);
        assertTrue(optimizer.getRejectedThisTick() > 0);

        optimizer.onWorldUnload();
        assertEquals(0, optimizer.getCurrentParticleCount());
        assertEquals(0, optimizer.getRejectedThisTick());
    }

    @Test
    void testDisabledOptimizationAllowsAll() {
        config.setParticleOptimizationEnabled(false);
        optimizer.setEffectiveBudget(1);

        assertTrue(optimizer.shouldAllowParticle(99999.0));
        assertTrue(optimizer.shouldAllowParticle(99999.0));
    }

    @Test
    void testBudgetClampingAndRecovery() {
        optimizer.setEffectiveBudget(10);
        assertEquals(50, optimizer.getEffectiveBudget());

        optimizer.setEffectiveBudget(500);
        assertEquals(500, optimizer.getEffectiveBudget());

        optimizer.updateParticleCount(300);
        assertEquals(300, optimizer.getCurrentParticleCount());

        optimizer.updateParticleCount(150);
        assertEquals(150, optimizer.getCurrentParticleCount());
    }
}
