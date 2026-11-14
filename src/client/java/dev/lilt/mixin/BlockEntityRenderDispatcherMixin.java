package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.entity.AnimationLodManager;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("unused")
@Mixin(value = BlockEntityRenderDispatcher.class, remap = false)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow
    public Camera camera;

    @ModifyVariable(
        method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private <E extends BlockEntity> float lilt$modifyPartialTick(float partialTick, E blockEntity) {
        if (blockEntity == null) return partialTick;

        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isAnimationLodEnabled()) return partialTick;

        BlockPos pos = blockEntity.getBlockPos();
        if (pos == null) return partialTick;

        Camera cam = this.camera;
        if (cam == null || !cam.isInitialized()) return partialTick;

        Vec3 camPos = cam.getPosition();
        Vector3f look = cam.getLookVector();

        double bx = pos.getX() + 0.5;
        double by = pos.getY() + 0.5;
        double bz = pos.getZ() + 0.5;

        boolean inFov = AnimationLodManager.isInFov(
            camPos.x, camPos.y, camPos.z,
            look.x(), look.y(), look.z(),
            bx, by, bz,
            0.15
        );

        double dx = bx - camPos.x;
        double dy = by - camPos.y;
        double dz = bz - camPos.z;
        double distSq = dx * dx + dy * dy + dz * dz;

        if (!inFov || distSq > 144.0) {
            boolean spiking = LiltClient.getAdaptiveController() != null && LiltClient.getAdaptiveController().isFrametimeSpiking();
            return AnimationLodManager.quantizeTick(partialTick, distSq, inFov, spiking);
        }
        return partialTick;
    }
}
