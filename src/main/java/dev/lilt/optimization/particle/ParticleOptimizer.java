package dev.lilt.optimization.particle;

import dev.lilt.config.LiltConfig;
import dev.lilt.compat.ModCompat;

public class ParticleOptimizer {

    public static final double NEAR_PROTECT_DISTANCE_SQ = 36.0;

    private final LiltConfig config;
    private final ModCompat modCompat;

    private volatile int effectiveBudget;
    private volatile int currentParticleCount = 0;
    private volatile int rejectedThisTick = 0;
    private volatile int totalRejected = 0;

    public ParticleOptimizer(LiltConfig config, ModCompat modCompat) {
        this.config = config;
        this.modCompat = modCompat;
        this.effectiveBudget = config.getParticleBudget();
    }

    public boolean shouldAllowParticle(double distanceSqToCamera) {
        return shouldAllowParticle(distanceSqToCamera, false);
    }

    public boolean shouldAllowParticle(double distanceSqToCamera, boolean isCritical) {
        if (!config.isParticleOptimizationEnabled()) {
            return true;
        }

        if (isCritical || distanceSqToCamera <= NEAR_PROTECT_DISTANCE_SQ) {
            currentParticleCount++;
            return true;
        }

        if (distanceSqToCamera > config.getParticleDistanceCullSq()) {
            rejectedThisTick++;
            totalRejected++;
            return false;
        }

        if (currentParticleCount >= effectiveBudget) {
            rejectedThisTick++;
            totalRejected++;
            return false;
        }

        currentParticleCount++;
        return true;
    }

    public void resetTickCounters() {
        rejectedThisTick = 0;
    }

    public void updateParticleCount(int count) {
        this.currentParticleCount = Math.max(0, count);
    }

    public void onWorldUnload() {
        this.currentParticleCount = 0;
        this.rejectedThisTick = 0;
    }

    public int getEffectiveBudget() { return effectiveBudget; }
    
    public void setEffectiveBudget(int budget) {
        this.effectiveBudget = Math.max(50, budget);
    }

    public int getCurrentParticleCount() { return currentParticleCount; }
    public int getRejectedThisTick() { return rejectedThisTick; }
    public int getTotalRejected() { return totalRejected; }
    public boolean isActive() { return config.isParticleOptimizationEnabled(); }
}
