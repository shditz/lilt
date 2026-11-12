package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.entity.AnimationLodManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("RETURN"))
    private void lilt$applyAnimationLod(T entity, S state, float partialTick, CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isAnimationLodEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (entity == mc.player || entity.isPassengerOfSameVehicle(mc.player))) {
            return;
        }

        double distSq = state.distanceToCameraSq;
        if (distSq > 144.0) {
            boolean spiking = LiltClient.getAdaptiveController() != null && LiltClient.getAdaptiveController().isFrametimeSpiking();
            state.ageInTicks = AnimationLodManager.quantizeTick(state.ageInTicks, distSq, spiking);
            state.walkAnimationPos = AnimationLodManager.quantizeTick(state.walkAnimationPos, distSq, spiking);
        }
    }
}
