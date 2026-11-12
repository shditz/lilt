package dev.lilt.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LiltConfigTest {

    @Test
    void testDefaultValues() {
        LiltConfig config = new LiltConfig();
        assertEquals("balanced", config.getActiveProfile());
        assertFalse(config.isOverlayEnabled());
        assertTrue(config.isAdaptiveEnabled());
        assertTrue(config.isParticleOptimizationEnabled());
        assertEquals(1000, config.getParticleBudget());
        assertTrue(config.isAnimationLodEnabled());
        assertTrue(config.isTextureCullingEnabled());
        assertTrue(config.isTickBurstProtectionEnabled());
        assertTrue(config.isGcGuardEnabled());
        assertTrue(config.isDiagnosticsEnabled());
    }

    @Test
    void testProfileSwitching(@TempDir Path tempDir) {
        Path configFile = tempDir.resolve("lilt.json");
        LiltConfig config = LiltConfig.load(configFile);

        config.setActiveProfile("performance");
        assertEquals("performance", config.getActiveProfile());
        assertEquals(2500, config.getParticleBudget());
        assertTrue(config.isAnimationLodEnabled());

        config.setActiveProfile("low");
        assertEquals("low", config.getActiveProfile());
        assertEquals(300, config.getParticleBudget());
        assertTrue(config.isAnimationLodEnabled());
    }

    @Test
    void testSaveAndReload(@TempDir Path tempDir) {
        Path configFile = tempDir.resolve("lilt.json");
        LiltConfig config = LiltConfig.load(configFile);
        config.setOverlayEnabled(true);
        config.setParticleBudget(750);
        config.setParticleDistanceCullSq(576.0);
        config.setAnimationLodEnabled(false);
        config.setTextureCullingEnabled(false);
        config.setTickBurstProtectionEnabled(false);
        config.setGcGuardEnabled(false);

        LiltConfig reloaded = LiltConfig.load(configFile);
        assertTrue(reloaded.isOverlayEnabled());
        assertEquals(750, reloaded.getParticleBudget());
        assertEquals(576.0, reloaded.getParticleDistanceCullSq());
        assertFalse(reloaded.isAnimationLodEnabled());
        assertFalse(reloaded.isTextureCullingEnabled());
        assertFalse(reloaded.isTickBurstProtectionEnabled());
        assertFalse(reloaded.isGcGuardEnabled());
        assertEquals("custom", reloaded.getActiveProfile());
    }

    @Test
    void testCorruptedConfigRecovery(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("lilt.json");
        Files.writeString(configFile, "{ invalid json content: true,,, }");

        LiltConfig config = LiltConfig.load(configFile);
        assertNotNull(config);
        assertEquals("balanced", config.getActiveProfile());

        Path broken = tempDir.resolve("lilt.json.broken");
        assertTrue(Files.exists(broken));
    }
}