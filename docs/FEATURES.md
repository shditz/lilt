# Features

## Performance Monitoring

Collects frame-level metrics using a ring buffer:

- Current FPS
- Average FPS (rolling window)
- Frame time (ms)
- P50 frame time
- P95 frame time
- P99 frame time
- 1% low FPS
- GC collection count and time
- Client tick duration

Configurable window size (default: 600 frames).

## Performance Overlay

Toggleable HUD (F7) showing:

- FPS with color coding (green/yellow/red)
- Frame presentation interval and percentiles (P50/P99)
- Heap usage and GC activity
- Client and integrated server tick timing
- GPU device and graphics backend
- Evidence-based pressure classification
- Active profile and adaptive controller state

Clean, minimal rendering via modern Fabric HUD element.

## Hardware Detection

Detects at startup:

- Logical processor count
- JVM version and max heap
- OS name and architecture

Detects safely via Minecraft RenderPearl API:

- GPU vendor and renderer
- Graphics backend (OpenGL / Vulkan)
- GPU vendor classification (NVIDIA/AMD/Intel/Apple/Unknown)

## Performance Diagnostics

Classifies performance pressure based on verifiable telemetry (120+ frames):

| Classification | Condition |
|---|---|
| CPU-bound | Tick overrun (>50ms client or integrated server) |
| GPU-bound | Verified asynchronous GPU timer query showing high GPU time |
| Memory | Heap usage >85% + frequent GC |
| Mixed | High frame time variance (P99/P50 > 3x) |
| None | No significant pressure |
| Unknown | Insufficient data or elevated frame times without direct GPU telemetry |

## Adaptive Controller

Automatically adjusts Lilt-controlled features:

- Uses FPS ratio (average FPS / target FPS) with hysteresis band
- Below threshold (default 85%): reduces particle budget by 20%
- Above threshold (default 95%): restores particle budget by 15%
- Cooldown between adjustments (default: 100 ticks / 5 seconds)
- Never touches Minecraft's own settings

## Particle Optimization

Budget-based particle management via Mixin:

- Configurable particle budget (default: 1000)
- Distance-based culling (default: 32 blocks)
- Near-player protection: particles within 6 blocks are never culled
- Lifecycle synchronization with living ParticleEngine particle counts
- Clean state reset on world unload and level transitions

## Configuration

JSON config at `.minecraft/config/lilt.json`:

- Schema versioning
- Profile presets (Low/Balanced/Performance/Custom)
- Individual feature toggles
- Value validation and clamping
- Broken config backup and recovery

## Mod Compatibility

Detects via Fabric Loader:

- Sodium, Iris, Lithium, FerriteCore
- ImmediatelyFast, Entity Culling, More Culling

No hard dependencies. No direct class references to optional mods.

