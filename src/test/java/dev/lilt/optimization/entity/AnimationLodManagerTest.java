package dev.lilt.optimization.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimationLodManagerTest {

    @Test
    void testGetLodIntervalNormal() {
        assertEquals(1, AnimationLodManager.getLodInterval(100.0, false));
        assertEquals(1, AnimationLodManager.getLodInterval(144.0, false));
        assertEquals(2, AnimationLodManager.getLodInterval(200.0, false));
        assertEquals(2, AnimationLodManager.getLodInterval(576.0, false));
        assertEquals(4, AnimationLodManager.getLodInterval(600.0, false));
        assertEquals(4, AnimationLodManager.getLodInterval(2500.0, false));
    }

    @Test
    void testGetLodIntervalSpiking() {
        assertEquals(2, AnimationLodManager.getLodInterval(100.0, true));
        assertEquals(2, AnimationLodManager.getLodInterval(144.0, true));
        assertEquals(4, AnimationLodManager.getLodInterval(200.0, true));
        assertEquals(4, AnimationLodManager.getLodInterval(576.0, true));
        assertEquals(8, AnimationLodManager.getLodInterval(600.0, true));
    }

    @Test
    void testGetLodIntervalOffScreen() {
        assertEquals(2, AnimationLodManager.getLodInterval(50.0, false, false));
        assertEquals(4, AnimationLodManager.getLodInterval(50.0, false, true));
        assertEquals(8, AnimationLodManager.getLodInterval(100.0, false, false));
        assertEquals(16, AnimationLodManager.getLodInterval(100.0, false, true));
    }

    @Test
    void testIsInFov() {
        assertTrue(AnimationLodManager.isInFov(0, 0, 0, 0, 0, 1, 0, 0, 10, 0.15));
        assertFalse(AnimationLodManager.isInFov(0, 0, 0, 0, 0, 1, 0, 0, -10, 0.15));
        assertTrue(AnimationLodManager.isInFov(0, 0, 0, 0, 0, 1, 0, 0, -1, 0.15));
        assertTrue(AnimationLodManager.isInFov(0, 0, 0, 0, 0, 1, 10, 0, 10, 0.15));
        assertFalse(AnimationLodManager.isInFov(0, 0, 0, 0, 0, 1, 10, 0, 0, 0.15));
    }

    @Test
    void testQuantizeTick() {
        assertEquals(15.75f, AnimationLodManager.quantizeTick(15.75f, 50.0, false));

        assertEquals(14.0f, AnimationLodManager.quantizeTick(15.75f, 200.0, false));
        assertEquals(16.0f, AnimationLodManager.quantizeTick(16.2f, 200.0, false));

        assertEquals(12.0f, AnimationLodManager.quantizeTick(15.75f, 800.0, false));
        assertEquals(16.0f, AnimationLodManager.quantizeTick(16.0f, 800.0, false));

        assertEquals(8.0f, AnimationLodManager.quantizeTick(15.75f, 800.0, true));
        assertEquals(16.0f, AnimationLodManager.quantizeTick(18.5f, 800.0, true));

        assertEquals(8.0f, AnimationLodManager.quantizeTick(15.75f, 100.0, false, false));
        assertEquals(0.0f, AnimationLodManager.quantizeTick(15.75f, 100.0, false, true));
    }
}
