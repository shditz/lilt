package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.optimization.particle.ParticleOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Integrates particle budget management into Minecraft's particle engine.
 * Uses @Inject with cancellable callback for distance and budget culling.
 */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void lilt$onTickStart(CallbackInfo ci) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer != null) {
            optimizer.resetTickCounters();
        }
    }

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void lilt$onCreateParticle(ParticleOptions options, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       CallbackInfoReturnable<Particle> cir) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer == null || !optimizer.isActive()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            double dx = x - client.player.getX();
            double dy = y - client.player.getY();
            double dz = z - client.player.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;

            if (!optimizer.shouldAllowParticle(distSq)) {
                cir.setReturnValue(null);
            }
        }
    }
}
