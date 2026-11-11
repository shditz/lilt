package dev.lilt.optimization.particle;

import dev.lilt.LiltMod;
import dev.lilt.config.LiltConfig;
import dev.lilt.compat.ModCompat;

/**
 * Manages particle workload by tracking the active particle count
 * and providing a budget-based culling decision.
 * 
 * This optimizer works by:
 * 1. Maintaining a per-tick count of active particles.
 * 2. Rejecting new particle additions when over budget.
 * 3. Distance-culling particles far from the camera.
 * 
 * Integrated via Mixin into the particle manager.
 */
public class ParticleOptimizer {

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

    /**
     * Called before adding a new particle.
     * Returns true if the particle should be allowed.
     */
    public boolean shouldAllowParticle(double distanceSqToCamera) {
        if (!config.isParticleOptimizationEnabled()) {
            return true;
        }

        // Distance culling
        if (distanceSqToCamera > config.getParticleDistanceCullSq()) {
            rejectedThisTick++;
            totalRejected++;
            return false;
        }

        // Budget check
        if (currentParticleCount >= effectiveBudget) {
            rejectedThisTick++;
            totalRejected++;
            return false;
        }

        currentParticleCount++;
        return true;
    }

    /**
     * Called at the start of each tick to reset counters.
     */
    public void resetTickCounters() {
        rejectedThisTick = 0;
    }

    /**
     * Called to sync the known particle count from the particle manager.
     */
    public void updateParticleCount(int count) {
        this.currentParticleCount = count;
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
