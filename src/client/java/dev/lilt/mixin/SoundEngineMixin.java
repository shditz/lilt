package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.sound.SoundLimiter;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = SoundEngine.class, remap = false)
public abstract class SoundEngineMixin {

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At("HEAD"), cancellable = true)
    private void lilt$throttleSound(SoundInstance soundInstance, CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        if (soundInstance == null) {
            return;
        }

        LiltConfig config = LiltClient.getConfig();
        if (config == null || config.getSoundLimit() <= 0) {
            return;
        }

        Identifier id = soundInstance.getIdentifier();
        if (id == null) {
            return;
        }

        if (SoundLimiter.shouldLimit(id.getPath(), soundInstance.getX(), soundInstance.getY(), soundInstance.getZ(), config.getSoundLimit())) {
            cir.setReturnValue(SoundEngine.PlayResult.NOT_STARTED);
        }
    }
}
