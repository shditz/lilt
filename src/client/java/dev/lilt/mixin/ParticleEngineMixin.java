package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.optimization.particle.ParticleOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    private Map<ParticleRenderType, ParticleGroup<?>> particles;

    @Shadow
    private Queue<Particle> particlesToAdd;

    @Unique
    private int lilt$calculateActiveParticles() {
        int count = 0;
        if (this.particles != null) {
            for (ParticleGroup<?> group : this.particles.values()) {
                if (group != null) {
                    count += group.size();
                }
            }
        }
        if (this.particlesToAdd != null) {
            count += this.particlesToAdd.size();
        }
        return count;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lilt$onTickStart(CallbackInfo ci) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer != null) {
            optimizer.resetTickCounters();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void lilt$onTickEnd(CallbackInfo ci) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer != null) {
            optimizer.updateParticleCount(lilt$calculateActiveParticles());
        }
    }

    @Inject(method = "clearParticles", at = @At("HEAD"))
    private void lilt$onClearParticles(CallbackInfo ci) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer != null) {
            optimizer.onWorldUnload();
        }
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void lilt$onSetLevel(ClientLevel level, CallbackInfo ci) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer != null) {
            optimizer.onWorldUnload();
        }
    }

    @Inject(method = "createParticle", at = @At("HEAD"), cancellable = true)
    private void lilt$onCreateParticle(ParticleOptions options, double x, double y, double z,
                                        double xSpeed, double ySpeed, double zSpeed,
                                        CallbackInfoReturnable<Particle> cir) {
        ParticleOptimizer optimizer = LiltClient.getParticleOptimizer();
        if (optimizer == null || !optimizer.isActive()) return;

        Minecraft client = Minecraft.getInstance();
        net.minecraft.world.entity.Entity camera = client.getCameraEntity() != null ? client.getCameraEntity() : client.player;
        if (camera != null) {
            double dx = x - camera.getX();
            double dy = y - camera.getY();
            double dz = z - camera.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;

            if (!optimizer.shouldAllowParticle(distSq)) {
                cir.setReturnValue(null);
            }
        }
    }
}
