package dev.lilt.optimization.shadow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShadowOptimizerTest {

    @Test
    void testOffOptionAlwaysCulls() {
        assertTrue(ShadowOptimizer.shouldCull(0.0, 0xF000F0, 0));
        assertTrue(ShadowOptimizer.shouldCull(100.0, 0xF000F0, 0));
    }

    @Test
    void testDistanceCulling() {
        int maxDist = 24;
        double insideDistSq = 20.0 * 20.0;
        double outsideDistSq = 25.0 * 25.0;

        int normalLight = (15 << 20) | (10 << 4);

        assertFalse(ShadowOptimizer.shouldCull(insideDistSq, normalLight, maxDist));
        assertTrue(ShadowOptimizer.shouldCull(outsideDistSq, normalLight, maxDist));
    }

    @Test
    void testCompleteDarknessCulling() {
        int pitchBlack = 0;
        int maxDist = 32;

        assertTrue(ShadowOptimizer.shouldCull(4.0, pitchBlack, maxDist));

        int withSkyLight = 5 << 20;
        assertFalse(ShadowOptimizer.shouldCull(4.0, withSkyLight, maxDist));

        int withBlockLight = 5 << 4;
        assertFalse(ShadowOptimizer.shouldCull(4.0, withBlockLight, maxDist));
    }

    @Test
    void testDefaultDistanceRespectsVanilla() {
        int normalLight = (15 << 20) | (10 << 4);
        assertFalse(ShadowOptimizer.shouldCull(10000.0, normalLight, -1));
    }
}
