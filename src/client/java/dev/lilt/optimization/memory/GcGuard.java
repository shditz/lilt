package dev.lilt.optimization.memory;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;

public final class GcGuard {

    private static long lastGcEpochMs = 0L;

    private GcGuard() {}

    public static void tryProactiveGc() {
        LiltConfig config = LiltClient.getConfig();
        if (config == null || !config.isGcGuardEnabled()) return;

        long now = System.currentTimeMillis();
        if (now - lastGcEpochMs < 30_000L) {
            return;
        }

        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        if (max > 0 && ((double) used / max) > 0.70) {
            lastGcEpochMs = now;
            System.gc();
        }
    }
}
