package dev.lilt.hardware;

import dev.lilt.LiltMod;
import org.lwjgl.opengl.GL11;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * Detects and exposes hardware information.
 * GPU queries must be performed on the render thread.
 */
public class HardwareInfo {

    private final int logicalProcessors;
    private final long maxHeapBytes;
    private final String jvmVersion;
    private final String osName;
    private final String osArch;

    // GPU info — populated lazily on the render thread
    private volatile String gpuVendor = "unknown";
    private volatile String gpuRenderer = "unknown";
    private volatile String glVersion = "unknown";
    private volatile boolean gpuQueried = false;

    public HardwareInfo() {
        this.logicalProcessors = Runtime.getRuntime().availableProcessors();
        this.maxHeapBytes = Runtime.getRuntime().maxMemory();
        this.jvmVersion = System.getProperty("java.version", "unknown");
        this.osName = System.getProperty("os.name", "unknown");
        this.osArch = System.getProperty("os.arch", "unknown");
    }

    /**
     * Must be called on the render thread (after OpenGL context is available).
     */
    public void queryGpuInfo() {
        if (gpuQueried) return;
        try {
            gpuVendor = GL11.glGetString(GL11.GL_VENDOR);
            gpuRenderer = GL11.glGetString(GL11.GL_RENDERER);
            glVersion = GL11.glGetString(GL11.GL_VERSION);
            gpuQueried = true;
            LiltMod.LOGGER.info("GPU: {} / {} (GL {})", gpuVendor, gpuRenderer, glVersion);
        } catch (Exception e) {
            LiltMod.LOGGER.warn("Failed to query GPU info", e);
        }
    }

    public GpuVendorType classifyGpuVendor() {
        if (gpuVendor == null) return GpuVendorType.UNKNOWN;
        String v = gpuVendor.toLowerCase();
        if (v.contains("nvidia")) return GpuVendorType.NVIDIA;
        if (v.contains("amd") || v.contains("ati")) return GpuVendorType.AMD;
        if (v.contains("intel")) return GpuVendorType.INTEL;
        if (v.contains("apple")) return GpuVendorType.APPLE;
        return GpuVendorType.UNKNOWN;
    }

    // Getters
    public int getLogicalProcessors() { return logicalProcessors; }
    public long getMaxHeapBytes() { return maxHeapBytes; }
    public long getMaxHeapMb() { return maxHeapBytes / (1024 * 1024); }
    public String getJvmVersion() { return jvmVersion; }
    public String getOsName() { return osName; }
    public String getOsArch() { return osArch; }
    public String getGpuVendor() { return gpuVendor; }
    public String getGpuRenderer() { return gpuRenderer; }
    public String getGlVersion() { return glVersion; }
    public boolean isGpuQueried() { return gpuQueried; }

    public long getUsedHeapMb() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        return memBean.getHeapMemoryUsage().getUsed() / (1024 * 1024);
    }

    public long getUsedHeapBytes() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    public enum GpuVendorType {
        NVIDIA, AMD, INTEL, APPLE, UNKNOWN
    }
}
