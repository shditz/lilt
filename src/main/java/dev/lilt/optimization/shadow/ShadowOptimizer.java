package dev.lilt.optimization.shadow;

public final class ShadowOptimizer {

    private ShadowOptimizer() {}

    public static boolean shouldCull(double distanceSq, int packedLight, int maxShadowDistance) {
        if (maxShadowDistance == 0) {
            return true;
        }
        if (maxShadowDistance > 0 && distanceSq > (double) (maxShadowDistance * maxShadowDistance)) {
            return true;
        }
        int blockLight = (packedLight >> 4) & 0xF;
        int skyLight = (packedLight >> 20) & 0xF;
        return blockLight == 0 && skyLight == 0;
    }
}
