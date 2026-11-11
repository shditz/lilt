package dev.lilt.adaptive;

import dev.lilt.LiltMod;
import dev.lilt.config.LiltConfig;
import dev.lilt.diagnostics.PerformanceDiagnostics;
import dev.lilt.monitor.PerformanceMonitor;
import dev.lilt.optimization.particle.ParticleOptimizer;

/**
 * Adaptive performance controller.
 * Uses rolling measurements, hysteresis, and cooldowns to adjust
 * Lilt-controlled features based on real performance data.
 * 
 * Never touches Minecraft's own settings (render distance, VSync, etc.).
 */
public class AdaptiveController {

    private final LiltConfig config;
    private final PerformanceMonitor monitor;
    private final PerformanceDiagnostics diagnostics;
    private final ParticleOptimizer particleOptimizer;

    private int cooldownTicks = 0;
    private AdaptiveState state = AdaptiveState.STABLE;
    private String lastAction = "none";

    public AdaptiveController(LiltConfig config, PerformanceMonitor monitor,
                              PerformanceDiagnostics diagnostics, ParticleOptimizer particleOptimizer) {
        this.config = config;
        this.monitor = monitor;
        this.diagnostics = diagnostics;
        this.particleOptimizer = particleOptimizer;
    }

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        // Need enough data before acting
        if (monitor.getFrameCount() < 60) return;

        double fpsRatio = monitor.getFpsRatio(config.getTargetFps());
        double hysteresisLow = config.getAdaptiveHysteresisLow();
        double hysteresisHigh = config.getAdaptiveHysteresisHigh();

        if (fpsRatio < hysteresisLow) {
            // Performance is below target — increase optimization
            if (state != AdaptiveState.REDUCING) {
                state = AdaptiveState.REDUCING;
                applyReduction();
                cooldownTicks = config.getAdaptiveCooldownTicks();
            }
        } else if (fpsRatio > hysteresisHigh) {
            // Performance is comfortably above target — relax optimization
            if (state != AdaptiveState.RELAXING) {
                state = AdaptiveState.RELAXING;
                applyRelaxation();
                cooldownTicks = config.getAdaptiveCooldownTicks();
            }
        } else {
            state = AdaptiveState.STABLE;
        }
    }

    private void applyReduction() {
        // Reduce particle budget by 20%
        int currentBudget = particleOptimizer.getEffectiveBudget();
        int newBudget = Math.max(100, (int) (currentBudget * 0.8));
        particleOptimizer.setEffectiveBudget(newBudget);
        lastAction = "Reduced particle budget to " + newBudget;
        LiltMod.LOGGER.debug("Adaptive: {}", lastAction);
    }

    private void applyRelaxation() {
        // Restore particle budget toward configured value
        int currentBudget = particleOptimizer.getEffectiveBudget();
        int configBudget = config.getParticleBudget();
        if (currentBudget < configBudget) {
            int newBudget = Math.min(configBudget, (int) (currentBudget * 1.15));
            particleOptimizer.setEffectiveBudget(newBudget);
            lastAction = "Restored particle budget to " + newBudget;
            LiltMod.LOGGER.debug("Adaptive: {}", lastAction);
        } else {
            state = AdaptiveState.STABLE;
            lastAction = "stable";
        }
    }

    public AdaptiveState getState() { return state; }
    public String getLastAction() { return lastAction; }

    public enum AdaptiveState {
        STABLE("Stable"),
        REDUCING("Reducing workload"),
        RELAXING("Relaxing limits");

        private final String displayName;
        AdaptiveState(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
