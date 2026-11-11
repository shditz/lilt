package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks into Minecraft's main client render loop.
 * Uses @Inject with minimal interference to record real frame intervals.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Unique
    private static long lilt$lastRenderFrameNanos = 0;

    @Inject(method = "renderFrame", at = @At("HEAD"))
    private void lilt$onRenderFrame(boolean renderLevel, CallbackInfo ci) {
        long now = System.nanoTime();
        if (lilt$lastRenderFrameNanos > 0 && LiltClient.getMonitor() != null) {
            LiltClient.getMonitor().recordFrame(now - lilt$lastRenderFrameNanos);
        }
        lilt$lastRenderFrameNanos = now;
    }
}
