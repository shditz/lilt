package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.shadow.ShadowOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(value = EntityRenderer.class, remap = false)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "extractShadow(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/Minecraft;Lnet/minecraft/world/level/Level;)V", at = @At("HEAD"), cancellable = true)
    private void lilt$cullShadow(S state, Minecraft minecraft, Level level, CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null) {
            return;
        }

        int maxDist = config.getShadowDistance();
        if (maxDist == -1) {
            return;
        }

        if (ShadowOptimizer.shouldCull(state.distanceToCameraSq, state.lightCoords, maxDist)) {
            state.shadowRadius = 0.0f;
            if (state.shadowPieces != null) {
                state.shadowPieces.clear();
            }
            ci.cancel();
        }
    }
}
