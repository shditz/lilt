package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.entity.AnimationLodManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
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

        if (mc.gameRenderer == null) return;
        Camera cam = mc.gameRenderer.getMainCamera();
        if (cam == null || !cam.isInitialized()) return;

        Vec3 camPos = cam.position();
        Vector3fc look = cam.forwardVector();

        boolean inFov = AnimationLodManager.isInFov(
            camPos.x, camPos.y, camPos.z,
            look.x(), look.y(), look.z(),
            entity.getX(), entity.getY(), entity.getZ(),
            0.15
        );

        double distSq = state.distanceToCameraSq;
        if (!inFov || distSq > 144.0) {
            boolean spiking = LiltClient.getAdaptiveController() != null && LiltClient.getAdaptiveController().isFrametimeSpiking();
            state.ageInTicks = AnimationLodManager.quantizeTick(state.ageInTicks, distSq, inFov, spiking);
            state.walkAnimationPos = AnimationLodManager.quantizeTick(state.walkAnimationPos, distSq, inFov, spiking);
        }
    }
}
