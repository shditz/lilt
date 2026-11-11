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
        assertEquals(60, config.getTargetFps());
        assertTrue(config.isParticleOptimizationEnabled());
        assertEquals(1000, config.getParticleBudget());
        assertTrue(config.isDiagnosticsEnabled());
    }

    @Test
    void testProfileSwitching(@TempDir Path tempDir) {
        Path configFile = tempDir.resolve("lilt.json");
        LiltConfig config = LiltConfig.load(configFile);

        config.setActiveProfile("performance");
        assertEquals("performance", config.getActiveProfile());
        assertEquals(500, config.getParticleBudget());

        config.setActiveProfile("low");
        assertEquals("low", config.getActiveProfile());
        assertEquals(2000, config.getParticleBudget());
        assertEquals(30, config.getTargetFps());
    }

    @Test
    void testSaveAndReload(@TempDir Path tempDir) {
        Path configFile = tempDir.resolve("lilt.json");
        LiltConfig config = LiltConfig.load(configFile);
        config.setOverlayEnabled(true);
        config.setParticleBudget(750);

        // Reload
        LiltConfig reloaded = LiltConfig.load(configFile);
        assertTrue(reloaded.isOverlayEnabled());
        assertEquals(750, reloaded.getParticleBudget());
    }

    @Test
    void testCorruptedConfigRecovery(@TempDir Path tempDir) throws IOException {
        Path configFile = tempDir.resolve("lilt.json");
        Files.writeString(configFile, "{ invalid json content: true,,, }");

        LiltConfig config = LiltConfig.load(configFile);
        assertNotNull(config);
        assertEquals("balanced", config.getActiveProfile());

        // Check broken backup file
        Path broken = tempDir.resolve("lilt.json.broken");
        assertTrue(Files.exists(broken));
    }
}

