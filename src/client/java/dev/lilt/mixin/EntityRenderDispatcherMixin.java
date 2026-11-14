package dev.lilt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.shadow.ShadowOptimizer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;FFLnet/minecraft/world/level/LevelReader;F)V", at = @At("HEAD"), cancellable = true)
    private static void lilt$cullShadow(PoseStack poseStack, MultiBufferSource bufferSource, EntityRenderState entityRenderState, float shadowAlpha, float partialTick, LevelReader levelReader, float shadowRadius, CallbackInfo ci) {
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

        if (entityRenderState != null && levelReader != null) {
            BlockPos pos = BlockPos.containing(entityRenderState.x, entityRenderState.y, entityRenderState.z);
            int packedLight = LevelRenderer.getLightColor(levelReader, pos);
            if (ShadowOptimizer.shouldCull(entityRenderState.distanceToCameraSq, packedLight, maxDist)) {
                ci.cancel();
            }
        }
    }
}
