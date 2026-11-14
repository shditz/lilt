package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.sound.SoundLimiter;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(SoundEngine.class)
public abstract class SoundEngineMixin {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void lilt$throttleSound(SoundInstance soundInstance, CallbackInfo ci) {
        if (soundInstance == null) {
            return;
        }

        LiltConfig config = LiltClient.getConfig();
        if (config == null || config.getSoundLimit() <= 0) {
            return;
        }

        ResourceLocation id = soundInstance.getLocation();
        if (id == null) {
            return;
        }

        if (SoundLimiter.shouldLimit(id.getPath(), soundInstance.getX(), soundInstance.getY(), soundInstance.getZ(), config.getSoundLimit())) {
            ci.cancel();
        }
    }
}
