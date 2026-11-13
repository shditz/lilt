package dev.lilt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.entity.AnimationLodManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void lilt$applyAnimationLod(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isAnimationLodEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (entity == mc.player || entity.isPassengerOfSameVehicle(mc.player))) {
            return;
        }

        double distSq = entity.distanceToSqr(mc.gameRenderer.getMainCamera().getPosition());
        if (distSq > 144.0) {
            boolean spiking = LiltClient.getAdaptiveController() != null && LiltClient.getAdaptiveController().isFrametimeSpiking();
            float currentPos = entity.walkAnimation.position(partialTicks);
            float quantized = AnimationLodManager.quantizeTick(currentPos, distSq, spiking);
            entity.walkAnimation.setSpeed(AnimationLodManager.quantizeTick(entity.walkAnimation.speed(), distSq, spiking));
        }
    }
}
