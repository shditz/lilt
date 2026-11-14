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
        assertEquals("medium", config.getActiveProfile());
        assertFalse(config.isOverlayEnabled());
        assertTrue(config.isAdaptiveEnabled());
        assertTrue(config.isParticleOptimizationEnabled());
        assertEquals(1500, config.getParticleBudget());
        assertTrue(config.isAnimationLodEnabled());
        assertTrue(config.isTextureCullingEnabled());
        assertTrue(config.isTickBurstProtectionEnabled());
        assertTrue(config.isGcGuardEnabled());
        assertTrue(config.isDiagnosticsEnabled());
        assertEquals(8, config.getSoundLimit());
        assertEquals(24, config.getShadowDistance());
        assertTrue(config.isBeaconCulling());
    }

    @Test
    void testProfileSwitching(@TempDir Path tempDir) {
        Path configFile = tempDir.resolve("lilt.json");
        LiltConfig config = LiltConfig.load(configFile);

        config.setActiveProfile("high");
        assertEquals("high", config.getActiveProfile());
        assertEquals(3000, config.getParticleBudget());
        assertEquals(0, config.getSoundLimit());
        assertEquals(-1, config.getShadowDistance());
        assertFalse(config.isBeaconCulling());

        config.setActiveProfile("low");
        assertEquals("low", config.getActiveProfile());
        assertEquals(500, config.getParticleBudget());
        assertEquals(2, config.getSoundLimit());
        assertEquals(0, config.getShadowDistance());
        assertTrue(config.isBeaconCulling());

        config.setActiveProfile("server");
        assertEquals("server", config.getActiveProfile());
        assertEquals(300, config.getParticleBudget());
        assertEquals(4, config.getSoundLimit());
        assertEquals(16, config.getShadowDistance());
        assertTrue(config.isBeaconCulling());
        assertFalse(config.isTickBurstProtectionEnabled());
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
        config.setSoundLimit(4);
        config.setShadowDistance(16);
        config.setBeaconCulling(false);

        LiltConfig reloaded = LiltConfig.load(configFile);
        assertTrue(reloaded.isOverlayEnabled());
        assertEquals(750, reloaded.getParticleBudget());
        assertEquals(576.0, reloaded.getParticleDistanceCullSq());
        assertFalse(reloaded.isAnimationLodEnabled());
        assertFalse(reloaded.isTextureCullingEnabled());
        assertFalse(reloaded.isTickBurstProtectionEnabled());
        assertFalse(reloaded.isGcGuardEnabled());
        assertEquals(4, reloaded.getSoundLimit());
        assertEquals(16, reloaded.getShadowDistance());
        assertFalse(reloaded.isBeaconCulling());
        assertEquals("custom", reloaded.getActiveProfile());
    }

    @Test
    void testLegacyProfileMigration(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("lilt.json");
        Files.writeString(configFile, "{\"schemaVersion\":2,\"activeProfile\":\"balanced\",\"particleBudget\":1000}");

        LiltConfig config = LiltConfig.load(configFile);
        assertEquals("medium", config.getActiveProfile());
    }

    @Test
    void testCorruptedConfigRecovery(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("lilt.json");
        Files.writeString(configFile, "{ invalid json content: true,,, }");

        LiltConfig config = LiltConfig.load(configFile);
        assertNotNull(config);
        assertEquals("medium", config.getActiveProfile());

        Path broken = tempDir.resolve("lilt.json.broken");
        assertTrue(Files.exists(broken));
    }
}
