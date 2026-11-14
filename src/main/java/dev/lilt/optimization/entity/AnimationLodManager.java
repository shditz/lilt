package dev.lilt.optimization.entity;

public final class AnimationLodManager {

    private AnimationLodManager() {}

    public static boolean isInFov(double camX, double camY, double camZ,
                                  float lookX, float lookY, float lookZ,
                                  double entX, double entY, double entZ,
                                  double fovCosineThreshold) {
        double dx = entX - camX;
        double dy = entY - camY;
        double dz = entZ - camZ;
        double distSq = dx * dx + dy * dy + dz * dz;
        if (distSq < 9.0) {
            return true;
        }
        double invDist = 1.0 / Math.sqrt(distSq);
        double dot = (dx * lookX + dy * lookY + dz * lookZ) * invDist;
        return dot >= fovCosineThreshold;
    }

    public static int getLodInterval(double distanceSq, boolean inFov, boolean spiking) {
        if (!inFov) {
            if (distanceSq <= 64.0) {
                return spiking ? 4 : 2;
            } else {
                return spiking ? 16 : 8;
            }
        }
        if (distanceSq <= 144.0) {
            return spiking ? 2 : 1;
        } else if (distanceSq <= 576.0) {
            return spiking ? 4 : 2;
        } else {
            return spiking ? 8 : 4;
        }
    }

    public static int getLodInterval(double distanceSq, boolean spiking) {
        return getLodInterval(distanceSq, true, spiking);
    }

    public static float quantizeTick(float tickValue, double distanceSq, boolean inFov, boolean spiking) {
        int interval = getLodInterval(distanceSq, inFov, spiking);
        if (interval <= 1) {
            return tickValue;
        }
        return ((int) (tickValue / interval)) * interval;
    }

    public static float quantizeTick(float tickValue, double distanceSq, boolean spiking) {
        return quantizeTick(tickValue, distanceSq, true, spiking);
    }
}
