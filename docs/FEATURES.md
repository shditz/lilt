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

Configurable window size (default: 300 frames).

## Performance Overlay

Toggleable HUD (F7) showing:

- FPS with color coding (green/yellow/red)
- Frame time percentiles
- Heap usage and GC activity
- Tick timing
- GPU info
- Pressure classification
- Active profile
- Adaptive controller state

Minimal rendering overhead — uses `DrawContext.drawText` directly.

## Hardware Detection

Detects at startup:

- Logical processor count
- JVM version and max heap
- OS name and architecture

Detects on first render (GL context required):

- GPU vendor and renderer
- OpenGL version
- GPU vendor classification (NVIDIA/AMD/Intel/Apple/Unknown)

## Performance Diagnostics

Classifies performance pressure after collecting sufficient data (120+ frames):

| Classification | Condition |
|---|---|
| CPU-bound | Tick overrun (>50ms) + high frame times |
| GPU-bound | High frame times with normal tick rate |
| Memory | Heap usage >85% + frequent GC |
| Mixed | High frame time variance (P99/P50 > 3x) |
| None | No significant pressure |
| Unknown | Insufficient data |

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
- Per-tick counter reset
- Tracks rejected particle count
- Cancellable — particles beyond budget or distance are skipped

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

