package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.optimization.memory.GcGuard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(value = Minecraft.class, remap = false)
public abstract class MinecraftMixin {

    @Unique
    private static long lilt$lastRenderFrameNanos = 0;

    @Unique
    private static long lilt$clientTickStartNanos = 0;

    @Unique
    private static long lilt$lastClientTickStartNanos = 0;

    @Inject(method = "renderFrame", at = @At("HEAD"))
    private void lilt$onRenderFrame(boolean renderLevel, CallbackInfo ci) {
        long now = System.nanoTime();
        if (lilt$lastRenderFrameNanos > 0 && LiltClient.getMonitor() != null) {
            LiltClient.getMonitor().recordFrame(now - lilt$lastRenderFrameNanos);
        }
        lilt$lastRenderFrameNanos = now;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lilt$onTickStart(CallbackInfo ci) {
        long now = System.nanoTime();
        lilt$clientTickStartNanos = now;
        if (lilt$lastClientTickStartNanos > 0 && LiltClient.getMonitor() != null) {
            LiltClient.getMonitor().recordClientTickInterval(now - lilt$lastClientTickStartNanos);
        }
        lilt$lastClientTickStartNanos = now;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void lilt$onTickReturn(CallbackInfo ci) {
        long now = System.nanoTime();
        if (lilt$clientTickStartNanos > 0 && LiltClient.getMonitor() != null) {
            LiltClient.getMonitor().recordClientTickExecution(now - lilt$clientTickStartNanos);
        }
    }

    @Inject(method = "setScreenAndShow", at = @At("HEAD"))
    private void lilt$onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof PauseScreen) {
            GcGuard.tryProactiveGc();
        }
    }
}