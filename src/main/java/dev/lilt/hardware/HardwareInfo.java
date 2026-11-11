package dev.lilt.hardware;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

public class HardwareInfo {

    private final int logicalProcessors;
    private final long maxHeapBytes;
    private final String jvmVersion;
    private final String osName;
    private final String osArch;

    private volatile String gpuVendor = "unavailable";
    private volatile String gpuRenderer = "unavailable";
    private volatile String graphicsBackend = "unavailable";
    private volatile boolean gpuQueried = false;

    public HardwareInfo() {
        this.logicalProcessors = Runtime.getRuntime().availableProcessors();
        this.maxHeapBytes = Runtime.getRuntime().maxMemory();
        this.jvmVersion = System.getProperty("java.version", "unavailable");
        this.osName = System.getProperty("os.name", "unavailable");
        this.osArch = System.getProperty("os.arch", "unavailable");
    }

    public void setGpuInfo(String vendor, String renderer, String backend) {
        this.gpuVendor = (vendor != null && !vendor.isBlank()) ? vendor : "unavailable";
        this.gpuRenderer = (renderer != null && !renderer.isBlank()) ? renderer : "unavailable";
        this.graphicsBackend = (backend != null && !backend.isBlank()) ? backend : "unavailable";
        this.gpuQueried = true;
    }

    public GpuVendorType classifyGpuVendor() {
        if (gpuVendor == null || gpuVendor.equals("unavailable")) return GpuVendorType.UNKNOWN;
        String v = gpuVendor.toLowerCase();
        if (v.contains("nvidia")) return GpuVendorType.NVIDIA;
        if (v.contains("amd") || v.contains("ati")) return GpuVendorType.AMD;
        if (v.contains("intel")) return GpuVendorType.INTEL;
        if (v.contains("apple")) return GpuVendorType.APPLE;
        return GpuVendorType.UNKNOWN;
    }

    public int getLogicalProcessors() { return logicalProcessors; }
    public long getMaxHeapBytes() { return maxHeapBytes; }
    public long getMaxHeapMb() { return maxHeapBytes / (1024 * 1024); }
    public String getJvmVersion() { return jvmVersion; }
    public String getOsName() { return osName; }
    public String getOsArch() { return osArch; }
    public String getGpuVendor() { return gpuVendor; }
    public String getGpuRenderer() { return gpuRenderer; }
    public String getGraphicsBackend() { return graphicsBackend; }
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
