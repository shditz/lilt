package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(value = IntegratedServer.class, remap = false)
public abstract class IntegratedServerMixin {

    @Unique
    private static final int MAX_CATCH_UP_TICKS = 3;

    @Unique
    private long lilt$lastTickEpochNanos = 0L;

    @Unique
    private int lilt$ticksInWindow = 0;

    @Inject(method = "tickServer", at = @At("HEAD"), cancellable = true)
    private void lilt$limitTickBurst(BooleanSupplier hasTime, CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isTickBurstProtectionEnabled()) return;

        long now = System.nanoTime();
        if (now - lilt$lastTickEpochNanos > 50_000_000L) {
            lilt$lastTickEpochNanos = now;
            lilt$ticksInWindow = 1;
        } else {
            lilt$ticksInWindow++;
            if (lilt$ticksInWindow > MAX_CATCH_UP_TICKS) {
                ci.cancel();
            }
        }
    }
}
