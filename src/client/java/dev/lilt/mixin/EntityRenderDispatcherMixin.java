package dev.lilt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.shadow.ShadowOptimizer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V", at = @At("HEAD"), cancellable = true)
    private static void lilt$cullShadow(PoseStack poseStack, MultiBufferSource bufferSource, Entity entity, float shadowAlpha, float partialTick, LevelReader levelReader, float shadowRadius, CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null) {
            return;
        }

        int maxDist = config.getShadowDistance();
        if (maxDist == -1) {
            return;
        }

        if (maxDist == 0) {
            ci.cancel();
            return;
        }

        if (entity != null && levelReader != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) {
                return;
            }
            Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
            double distSq = entity.distanceToSqr(camPos);
            BlockPos pos = entity.blockPosition();
            int packedLight = LevelRenderer.getLightColor(levelReader, pos);
            if (ShadowOptimizer.shouldCull(distSq, packedLight, maxDist)) {
                ci.cancel();
            }
        }
    }
}
