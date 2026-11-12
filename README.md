<p align="center">
  <img src="https://raw.githubusercontent.com/shditz/lilt/main/assets/banner.png" alt="Lilt Banner" width="100%">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/shditz/lilt/main/assets/icon.png" alt="Lilt Icon" width="96" height="96"><br>
  <strong>A client-side frame stabilizer and workload optimizer for Minecraft Java Edition (Fabric 26.3).</strong>
</p>

---

## Overview

Lilt targets the real causes of sudden framerate drops and multi-second freezes on budget hardware, laptops, and heavily modded setups. Rather than replacing existing chunk rendering engines like Sodium, Lilt works alongside them to stabilize everything outside block rendering: mob animation computational overhead, singleplayer tick spirals, texture upload bus congestion, and memory garbage collection pauses.

---

## What Problem Does Lilt Solve?

Modern rendering mods (such as Sodium) solve block rendering performance, but players on budget laptops and mid-range PCs still frequently experience:
- **Catastrophic drops down to 1–5 FPS** when approaching villages, mob farms, or large animal pens.
- **Sudden 1–2 second freezes** when exploring the world or generating new chunks in singleplayer.
- **Micro-stutters and hitching** from constant animated texture uploads across PCIe channels and stop-the-world Java GC cycles.

Lilt directly addresses each of these bottlenecks with lightweight, non-invasive optimizations.

---

## Features

### 1. Distance-Adaptive Entity Animation LOD
In vanilla Minecraft, the client computes full bone transformations, rotations, and limb kinematics for every mob on every rendered frame, regardless of how far the mob is from the camera.
- **Near (<12m)**: Full 60+ FPS fidelity. All player-adjacent animations remain completely smooth.
- **Mid-range (12–24m)**: Animations are quantized to 30 FPS updates.
- **Far (>24m)**: Animations are quantized to 15 FPS updates.
- **Under Severe Load**: When a frametime spike is actively detected, far-distance updates are temporarily throttled further.
- **Player Immunity**: The local player and ridden mounts are never throttled.

### 2. Singleplayer Tick Burst Limiter
In singleplayer, Minecraft runs an internal server in a background thread. During world exploration and chunk loading, the internal server can fall behind real-time. In vanilla, the server responds by attempting to catch up in a tight, unthrottled loop of back-to-back ticks. On 4-core and 8-thread CPUs, this burst starves the client render thread of CPU resources, causing the screen to freeze for 1 to 2 seconds.
- Lilt limits internal catch-up tick bursts to a maximum of 3 ticks per 50ms window.
- Lilt elevates the client render thread priority to ensure frame presentation is never starved by background chunk tasks.

### 3. Visibility-Aware Texture Animation Optimization
Animated textures (water, lava, portal blocks, modded machinery) continuously copy texture frames to the GPU across the PCIe bus.
- Pauses texture uploads entirely while the game is paused or in menus.
- When an active frametime spike is detected, texture uploads are staggered to alternating frames, cutting PCIe bandwidth consumption by half without halting in-game animations.

### 4. Proactive Memory / GC Guard
Stop-the-world garbage collection pauses during boss fights or active combat ruin gameplay.
- Monitors JVM heap usage without allocating memory.
- If heap consumption exceeds 70% of maximum capacity, Lilt triggers a proactive garbage collection cycle only when the player enters the Pause/ESC menu.
- Ensures memory is cleared safely while no active rendering or combat is occurring.

### 5. Dynamic Frametime Spike Detection
Lilt does not rely on a static "Target FPS" setting that creates false triggers when framerates fluctuate.
- Calculates rolling frametime ratios ($P_{99} / P_{50}$) and instantaneous spike factors.
- Detects genuine stutters dynamically whether your game is running at 30, 60, 144 FPS, or Unlimited.
- Dynamically throttles particle budgets and signals other subsystems during stress, then smoothly restores full quality once frame delivery stabilizes.

### 6. Seamless Video Settings Integration
- **Vanilla Video Settings**: Lilt adds a dedicated, cleanly styled options section directly within the standard scrollable settings list. Changing the profile preset instantly updates all widgets on-screen.
- **Sodium Video Settings**: Fully integrates into Sodium's native sidebar menu with a dedicated "Lilt" tab, matching Sodium's native UI standards and tooltips.

### 7. Telemetry HUD (F7)
A zero-allocation HUD overlay displaying:
- Real-time FPS and average framerate
- Percentile frame presentation intervals (P50, P95, P99)
- 1% low framerate
- Client tick execution duration
- Integrated server tick time (in singleplayer)
- Live JVM heap utilization and recent GC pauses
- Hardware telemetry: GPU device name, driver, and active graphics backend

---

## Mod Compatibility

Lilt is built strictly following Fabric and Mixin best practices. It avoids `@Overwrite` and does not replace vanilla or Sodium rendering pipelines.

| Mod | Compatibility | Status |
|---|---|---|
| **Sodium** | 100% Compatible | Coexists seamlessly; registers a native settings tab via Sodium Config API |
| **Lithium** | 100% Compatible | Zero overlap; Lithium optimizes server physics, Lilt stabilizes client workloads |
| **FerriteCore** | 100% Compatible | Complementary memory reductions |
| **EntityCulling** | 100% Compatible | EntityCulling hides occluded mobs; Lilt optimizes animation LOD for visible ones |
| **ImmediatelyFast** | 100% Compatible | Compatible with immediate-mode buffer batching |
| **Iris Shaders** | 100% Compatible | Fully functional alongside shader pipelines |

---

## Configuration Profiles

Lilt can be configured directly in **Options → Video Settings** (or through `.minecraft/config/lilt.json`).

| Setting | Low Profile | Balanced (Default) | Performance Profile | Custom |
|---|---|---|---|---|
| **Adaptive Tuning** | Enabled | Enabled | Enabled | User-defined |
| **Animation LOD** | Enabled | Enabled | Enabled | User-defined |
| **Texture Optimization** | Enabled | Enabled | Enabled | User-defined |
| **Tick Burst Limiter** | Enabled | Enabled | Enabled | User-defined |
| **Memory Guard** | Enabled | Enabled | Enabled | User-defined |
| **Particle Culling** | Enabled | Enabled | Enabled | User-defined |
| **Particle Budget** | 300 | 1000 | 2500 | User-defined |
| **Cull Distance** | 20 blocks | 32 blocks | 64 blocks | User-defined |
| **Target Hardware** | Low-end laptops, iGPUs | Mid-range PCs, standard laptops | High-end desktop gaming rigs | Custom preferences |

---

## Requirements

- **Minecraft**: Java Edition 26.3
- **Loader**: Fabric Loader ≥ 0.19.5
- **Java**: 25
- **Dependencies**: Fabric API

---

## Building from Source

Prerequisites: JDK 25 installed.

```bash
git clone https://github.com/shditz/lilt.git
cd lilt
./gradlew build
```

Compiled JAR files are produced in `build/libs/`:
- `lilt-<version>.jar` (production mod binary)
- `lilt-<version>-sources.jar` (source bundle)

---

## License

This project is licensed under the [MIT License](LICENSE).
