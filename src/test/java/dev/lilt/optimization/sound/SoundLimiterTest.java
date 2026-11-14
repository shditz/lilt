package dev.lilt.optimization.sound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SoundLimiterTest {

    @BeforeEach
    void setUp() {
        SoundLimiter.reset();
    }

    @Test
    void testTargetSounds() {
        assertTrue(SoundLimiter.isTargetSound("entity.zombie.ambient"));
        assertTrue(SoundLimiter.isTargetSound("entity.cow.ambient"));
        assertTrue(SoundLimiter.isTargetSound("entity.skeleton.hurt"));
        assertTrue(SoundLimiter.isTargetSound("entity.sheep.step"));
        assertTrue(SoundLimiter.isTargetSound("block.piston.extend"));
        assertTrue(SoundLimiter.isTargetSound("block.piston.contract"));

        assertFalse(SoundLimiter.isTargetSound("entity.player.attack.crit"));
        assertFalse(SoundLimiter.isTargetSound("entity.generic.explode"));
        assertFalse(SoundLimiter.isTargetSound("ui.button.click"));
        assertFalse(SoundLimiter.isTargetSound("music.game"));
        assertFalse(SoundLimiter.isTargetSound(null));
        assertFalse(SoundLimiter.isTargetSound(""));
    }

    @Test
    void testUnlimitedLimit() {
        for (int i = 0; i < 20; i++) {
            assertFalse(SoundLimiter.shouldLimit("entity.zombie.ambient", 10.0, 64.0, 10.0, 0));
        }
    }

    @Test
    void testThrottlingWithinWindow() {
        String sound = "entity.cow.ambient";
        double x = 100.0;
        double y = 64.0;
        double z = 100.0;
        int limit = 4;

        for (int i = 0; i < limit; i++) {
            assertFalse(SoundLimiter.shouldLimit(sound, x, y, z, limit));
        }

        assertTrue(SoundLimiter.shouldLimit(sound, x, y, z, limit));
        assertTrue(SoundLimiter.shouldLimit(sound, x, y, z, limit));
    }

    @Test
    void testDifferentLocationsDoNotThrottleEachOther() {
        String sound = "entity.cow.ambient";
        int limit = 2;

        assertFalse(SoundLimiter.shouldLimit(sound, 0.0, 64.0, 0.0, limit));
        assertFalse(SoundLimiter.shouldLimit(sound, 0.0, 64.0, 0.0, limit));
        assertTrue(SoundLimiter.shouldLimit(sound, 0.0, 64.0, 0.0, limit));

        assertFalse(SoundLimiter.shouldLimit(sound, 256.0, 64.0, 256.0, limit));
        assertFalse(SoundLimiter.shouldLimit(sound, 256.0, 64.0, 256.0, limit));
    }
}
