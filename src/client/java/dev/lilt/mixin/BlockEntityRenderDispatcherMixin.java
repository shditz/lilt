package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import dev.lilt.optimization.entity.AnimationLodManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("unused")
@Mixin(value = BlockEntityRenderDispatcher.class, remap = false)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "tryExtractRenderState", at = @At("RETURN"))
    private void lilt$applyBlockAnimationLod(CallbackInfoReturnable<BlockEntityRenderState> cir) {
        BlockEntityRenderState state = cir.getReturnValue();
        if (state == null) return;

        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isAnimationLodEnabled()) return;

        BlockPos pos = state.blockPos;
        if (pos == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) return;
        Camera cam = mc.gameRenderer.getMainCamera();
        if (cam == null || !cam.isInitialized()) return;

        Vec3 camPos = cam.position();
        Vector3fc look = cam.forwardVector();

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

            if (state instanceof BannerRenderState bannerState) {
                bannerState.phase = AnimationLodManager.quantizeTick(bannerState.phase, distSq, inFov, spiking);
            } else if (state instanceof BellRenderState bellState) {
                bellState.ticks = AnimationLodManager.quantizeTick(bellState.ticks, distSq, inFov, spiking);
            } else if (state instanceof ChestRenderState chestState) {
                if (chestState.open > 0.0f && chestState.open < 1.0f) {
                    chestState.open = AnimationLodManager.quantizeTick(chestState.open * 10.0f, distSq, inFov, spiking) * 0.1f;
                }
            }
        }
    }
}
