package dev.lilt.optimization.entity;

public final class AnimationLodManager {

    private AnimationLodManager() {}

    public static int getLodInterval(double distanceSq, boolean spiking) {
        if (distanceSq <= 144.0) {
            return spiking ? 2 : 1;
        } else if (distanceSq <= 576.0) {
            return spiking ? 4 : 2;
        } else {
            return spiking ? 8 : 4;
        }
    }

    public static float quantizeTick(float tickValue, double distanceSq, boolean spiking) {
        int interval = getLodInterval(distanceSq, spiking);
        if (interval <= 1) {
            return tickValue;
        }
        return ((int) (tickValue / interval)) * interval;
    }
}
