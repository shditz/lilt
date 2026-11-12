package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class TextureAtlasMixin {

    @Unique
    private static int lilt$frameCounter = 0;

    @Inject(method = "cycleAnimationFrames()V", at = @At("HEAD"), cancellable = true)
    private void lilt$throttleTextureAnimations(CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isTextureCullingEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) {
            ci.cancel();
            return;
        }

        boolean severeSpike = LiltClient.getAdaptiveController() != null && LiltClient.getAdaptiveController().isSevereSpike();
        if (severeSpike) {
            lilt$frameCounter++;
            if ((lilt$frameCounter & 1) != 0) {
                ci.cancel();
            }
        }
    }
}
