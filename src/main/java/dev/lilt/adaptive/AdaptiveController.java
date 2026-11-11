package dev.lilt.adaptive;

import dev.lilt.LiltMod;
import dev.lilt.config.LiltConfig;
import dev.lilt.diagnostics.PerformanceDiagnostics;
import dev.lilt.monitor.PerformanceMonitor;
import dev.lilt.optimization.particle.ParticleOptimizer;

public class AdaptiveController {

    public static final int MIN_PARTICLE_BUDGET = 100;

    private final LiltConfig config;
    private final PerformanceMonitor monitor;
    private final PerformanceDiagnostics diagnostics;
    private final ParticleOptimizer particleOptimizer;

    private int cooldownTicks = 0;
    private AdaptiveState state = AdaptiveState.STABLE;
    private String lastAction = "none";
    private int userFramerateCap = 0;

    public AdaptiveController(LiltConfig config, PerformanceMonitor monitor,
                              PerformanceDiagnostics diagnostics, ParticleOptimizer particleOptimizer) {
        this.config = config;
        this.monitor = monitor;
        this.diagnostics = diagnostics;
        this.particleOptimizer = particleOptimizer;
    }

    public void setUserFramerateCap(int cap) {
        this.userFramerateCap = Math.max(0, cap);
    }

    public int getUserFramerateCap() {
        return userFramerateCap;
    }

    public int getEffectiveTargetFps() {
        int target = config.getTargetFps();
        if (userFramerateCap > 0 && userFramerateCap < 260) {
            target = Math.min(target, userFramerateCap);
        }
        return target;
    }

    public void tick() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        if (monitor.getFrameCount() < 60) return;

        int effectiveTarget = getEffectiveTargetFps();
        double fpsRatio = monitor.getFpsRatio(effectiveTarget);
        double hysteresisLow = config.getAdaptiveHysteresisLow();
        double hysteresisHigh = config.getAdaptiveHysteresisHigh();

        if (fpsRatio < hysteresisLow) {
            if (!isParticleReductionRelevant()) {
                state = AdaptiveState.STABLE;
                lastAction = "Suppressed reduction: " + diagnostics.getCurrentPressure().getDisplayName();
                cooldownTicks = config.getAdaptiveCooldownTicks();
                return;
            }

            state = AdaptiveState.REDUCING;
            applyReduction();
            cooldownTicks = config.getAdaptiveCooldownTicks();
        } else if (fpsRatio > hysteresisHigh) {
            int currentBudget = particleOptimizer.getEffectiveBudget();
            int configBudget = config.getParticleBudget();

            if (currentBudget < configBudget) {
                state = AdaptiveState.RELAXING;
                applyRelaxation();
                cooldownTicks = config.getAdaptiveCooldownTicks();
            } else {
                state = AdaptiveState.STABLE;
                lastAction = "stable";
            }
        } else {
            state = AdaptiveState.STABLE;
        }
    }

    private boolean isParticleReductionRelevant() {
        PerformanceDiagnostics.PressureType pressure = diagnostics.getCurrentPressure();
        if (pressure == PerformanceDiagnostics.PressureType.CPU_BOUND ||
            pressure == PerformanceDiagnostics.PressureType.MEMORY ||
            pressure == PerformanceDiagnostics.PressureType.NONE) {
            return false;
        }
        if (diagnostics.isGpuTimingSupported() && pressure == PerformanceDiagnostics.PressureType.UNKNOWN) {
            return false;
        }
        return true;
    }

    private void applyReduction() {
        int currentBudget = particleOptimizer.getEffectiveBudget();
        int minBudget = Math.min(MIN_PARTICLE_BUDGET, config.getParticleBudget());

        if (currentBudget > minBudget) {
            int newBudget = Math.max(minBudget, (int) (currentBudget * 0.8));
            particleOptimizer.setEffectiveBudget(newBudget);
            lastAction = "Reduced particle budget to " + newBudget;
            LiltMod.LOGGER.debug("Adaptive: {}", lastAction);
        } else {
            lastAction = "Particle budget at minimum (" + minBudget + ")";
        }
    }

    private void applyRelaxation() {
        int currentBudget = particleOptimizer.getEffectiveBudget();
        int configBudget = config.getParticleBudget();
        if (currentBudget < configBudget) {
            int step = Math.max(10, (int) (currentBudget * 0.15));
            int newBudget = Math.min(configBudget, currentBudget + step);
            particleOptimizer.setEffectiveBudget(newBudget);
            if (newBudget >= configBudget) {
                state = AdaptiveState.STABLE;
                lastAction = "Restored particle budget to max";
            } else {
                lastAction = "Restored particle budget to " + newBudget;
            }
            LiltMod.LOGGER.debug("Adaptive: {}", lastAction);
        } else {
            state = AdaptiveState.STABLE;
            lastAction = "stable";
        }
    }

    public AdaptiveState getState() { return state; }
    public String getLastAction() { return lastAction; }
    public int getCooldownTicks() { return cooldownTicks; }

    public enum AdaptiveState {
        STABLE("Stable"),
        REDUCING("Reducing workload"),
        RELAXING("Relaxing limits");

        private final String displayName;
        AdaptiveState(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
}
