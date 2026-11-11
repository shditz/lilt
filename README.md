# Lilt

**A steadier way to play.**

Lilt is a client-side performance monitor and workload stabilizer for Minecraft Java Edition (Fabric 26.3). It tracks real-time frametimes, garbage collection pauses, and tick durations, while dynamically balancing particle loads to eliminate micro-stutters during heavy scenes like mob farms, nether fires, and explosions.

Designed to operate seamlessly alongside rendering mods like Sodium and Iris.

## Features

- **Performance telemetry** — Real-time FPS, frame presentation intervals (P50/P95/P99), 1% lows, GC pause tracking, and client tick execution timing
- **Low-overhead HUD (F7)** — Carbon-style telemetry overlay cached at 250ms with zero per-frame CPU allocation
- **Hardware diagnosis** — GPU device detection via RenderPearl, graphics backend telemetry, and bottleneck classification
- **Adaptive workload controller** — Automatically trims distant particle workloads during frametime spikes and restores them when stable
- **Particle management** — Global particle budget limits with distance-based culling and 6-block near-player visual protection
- **In-game settings screen** — Accessible directly from **Video Settings → Lilt Options...** with detailed setting descriptions and performance advice
- **Configuration profiles** — Balanced (default), Low (low-end hardware), Performance (gaming rigs), and Custom
- **Mod ecosystem compatibility** — Fully compatible with Sodium, Lithium, Iris, FerriteCore, and ImmediatelyFast

## Requirements

- Minecraft Java Edition 26.3
- Java 25
- Fabric Loader ≥ 0.19.5
- Fabric API

## Installation

1. Install Fabric Loader for Minecraft 26.3
2. Place `lilt-0.1.0-dev.2.jar` into your `.minecraft/mods` directory
3. Launch Minecraft

## Configuration

Configure in-game directly via **Options → Video Settings → Lilt Options...**, or edit `.minecraft/config/lilt.json`.

### Profiles

| Profile | Target FPS | Particle Budget | Particle Cull Distance | Target Hardware |
|---------|-----------|-----------------|------------------------|-----------------|
| Low | 30 | 300 | 20 blocks | Older laptops, integrated graphics |
| Balanced | 60 | 1000 | 32 blocks | Standard PCs, balanced visuals & performance |
| Performance | 120 | 2500 | 64 blocks | High-end gaming PCs with dedicated GPUs |
| Custom | User-defined | User-defined | User-defined | User-configured preferences |

## Controls

| Key | Action |
|-----|--------|
| F7 | Toggle performance telemetry HUD |

## Building

```bash
./gradlew build
```

Output JAR: `build/libs/lilt-0.1.0-dev.2.jar`

## License

MIT
