package dev.lilt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(value = BeaconRenderer.class, remap = false)
public abstract class BeaconRendererMixin {

    @Inject(method = "submit(Lnet/minecraft/client/renderer/blockentity/state/BeaconRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void lilt$cullBeaconBeam(BeaconRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (state == null) {
            return;
        }

        BlockPos pos = state.blockPos;
        if (pos == null) {
            return;
        }

        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isBeaconCulling()) {
            return;
        }

        if (cameraRenderState != null && cameraRenderState.cullFrustum != null && cameraRenderState.pos != null) {
            double dx = (pos.getX() + 0.5) - cameraRenderState.pos.x;
            double dz = (pos.getZ() + 0.5) - cameraRenderState.pos.z;
            if (dx * dx + dz * dz < 64.0) {
                return;
            }

            AABB beamAabb = new AABB(
                pos.getX() - 1.0, pos.getY(), pos.getZ() - 1.0,
                pos.getX() + 2.0, pos.getY() + 384.0, pos.getZ() + 2.0
            );

            if (!cameraRenderState.cullFrustum.isVisible(beamAabb)) {
                ci.cancel();
            }
        }
    }
}
