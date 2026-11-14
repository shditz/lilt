package dev.lilt.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.entity.AnimationLodManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("unused")
@Mixin(BeaconRenderer.class)
public abstract class BeaconRendererMixin {

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), cancellable = true)
    private void lilt$cullBeaconBeam(BeaconBlockEntity beacon, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, CallbackInfo ci) {
        if (beacon == null) {
            return;
        }

        BlockPos pos = beacon.getBlockPos();
        if (pos == null) {
            return;
        }

        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isBeaconCulling()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) {
            return;
        }
        Camera cam = mc.gameRenderer.getMainCamera();
        if (cam == null || !cam.isInitialized()) {
            return;
        }

        Vec3 camPos = cam.getPosition();
        double bx = pos.getX() + 0.5;
        double bz = pos.getZ() + 0.5;

        double dx = bx - camPos.x;
        double dz = bz - camPos.z;
        if (dx * dx + dz * dz < 64.0) {
            return;
        }

        Vector3f look = cam.getLookVector();
        boolean inFov = AnimationLodManager.isInFov(
            camPos.x, camPos.y, camPos.z,
            look.x(), look.y(), look.z(),
            bx, camPos.y, bz,
            -0.2
        );

        if (!inFov) {
            ci.cancel();
        }
    }
}
