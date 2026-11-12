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
    void testQuantizeTick() {
        assertEquals(15.75f, AnimationLodManager.quantizeTick(15.75f, 50.0, false));

        assertEquals(14.0f, AnimationLodManager.quantizeTick(15.75f, 200.0, false));
        assertEquals(16.0f, AnimationLodManager.quantizeTick(16.2f, 200.0, false));

        assertEquals(12.0f, AnimationLodManager.quantizeTick(15.75f, 800.0, false));
        assertEquals(16.0f, AnimationLodManager.quantizeTick(16.0f, 800.0, false));

        assertEquals(8.0f, AnimationLodManager.quantizeTick(15.75f, 800.0, true));
        assertEquals(16.0f, AnimationLodManager.quantizeTick(18.5f, 800.0, true));
    }
}
