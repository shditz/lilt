package dev.lilt.optimization.sound;

public final class SoundLimiter {

    private SoundLimiter() {}

    private static final int TABLE_SIZE = 512;
    private static final int MASK = TABLE_SIZE - 1;

    private static final long[] KEYS = new long[TABLE_SIZE];
    private static final long[] WINDOW_STARTS = new long[TABLE_SIZE];
    private static final int[] COUNTS = new int[TABLE_SIZE];

    public static boolean shouldLimit(String soundPath, double x, double y, double z, int limitPerSecond) {
        if (limitPerSecond <= 0) {
            return false;
        }
        if (!isTargetSound(soundPath)) {
            return false;
        }

        int chunkX = (int) Math.floor(x) >> 4;
        int chunkZ = (int) Math.floor(z) >> 4;

        long key = (((long) soundPath.hashCode()) << 32) ^ (((long) chunkX) << 16) ^ (chunkZ & 0xFFFFL);
        int index = (int) ((key ^ (key >>> 16)) & MASK);

        long now = System.currentTimeMillis();
        long start = WINDOW_STARTS[index];

        if (KEYS[index] != key || (now - start) >= 1000L) {
            KEYS[index] = key;
            WINDOW_STARTS[index] = now;
            COUNTS[index] = 1;
            return false;
        }

        int count = COUNTS[index];
        if (count >= limitPerSecond) {
            return true;
        }

        COUNTS[index] = count + 1;
        return false;
    }

    public static boolean isTargetSound(String soundPath) {
        if (soundPath == null || soundPath.isEmpty()) {
            return false;
        }
        if (soundPath.startsWith("entity.")) {
            return soundPath.contains("ambient") || soundPath.contains("hurt") || soundPath.contains("step");
        }
        return soundPath.startsWith("block.piston");
    }

    public static void reset() {
        for (int i = 0; i < TABLE_SIZE; i++) {
            KEYS[i] = 0;
            WINDOW_STARTS[i] = 0;
            COUNTS[i] = 0;
        }
    }
}
