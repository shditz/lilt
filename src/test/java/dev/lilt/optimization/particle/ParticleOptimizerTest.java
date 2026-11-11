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
        // default: budget = 1000, distanceCullSq = 1024.0 (32 blocks)
        optimizer = new ParticleOptimizer(config, new ModCompat());
    }

    @Test
    void testAllowsParticleWithinBudgetAndDistance() {
        assertTrue(optimizer.shouldAllowParticle(100.0)); // 10 blocks away
        assertEquals(1, optimizer.getCurrentParticleCount());
        assertEquals(0, optimizer.getRejectedThisTick());
    }

    @Test
    void testRejectsParticleExceedingDistance() {
        // Distance squared > 1024.0
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

        // Particle beyond budget should be rejected
        assertFalse(optimizer.shouldAllowParticle(50.0));
        assertEquals(1, optimizer.getRejectedThisTick());
        assertEquals(1, optimizer.getTotalRejected());
    }

    @Test
    void testDisabledOptimizationAllowsAll() {
        config.setParticleOptimizationEnabled(false);
        optimizer.setEffectiveBudget(1);

        assertTrue(optimizer.shouldAllowParticle(99999.0)); // far distance
        assertTrue(optimizer.shouldAllowParticle(99999.0)); // over budget
    }
}
