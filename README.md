# Lilt

**A steadier way to play.**

Lilt is a client-side performance mod for Minecraft Java Edition. It monitors how the game runs, identifies performance pressure, and helps tune selected workloads to keep gameplay responsive across different hardware.

Built for Fabric, targeting Minecraft 26.3.

## Features

- **Performance monitoring** — FPS, frame times (P50/P95/P99), 1% lows, GC activity, tick timing
- **Performance overlay** — toggleable HUD showing live metrics (F7)
- **Hardware detection** — CPU cores, GPU info, JVM heap, GL capabilities
- **Performance diagnostics** — classifies pressure as CPU-bound, GPU-bound, memory, or mixed
- **Adaptive controller** — adjusts Lilt's own settings based on real performance data
- **Particle optimization** — budget-based particle management with distance culling
- **Mod compatibility** — detects Sodium, Lithium, Iris, and other performance mods
- **Configuration profiles** — Low, Balanced (default), Performance, Custom
- **Persistent JSON config** — schema versioning, validation, safe recovery

## Requirements

- Minecraft Java Edition 26.3
- Java 25
- Fabric Loader ≥ 0.19.5
- Fabric API

## Installation

1. Install Fabric Loader for Minecraft 26.3
2. Drop `lilt-0.1.0-dev.1.jar` into your `mods/` folder
3. Launch the game

## Configuration

Config is stored at `.minecraft/config/lilt.json`. Edit it directly or change the profile in-game.

### Profiles

| Profile | Target FPS | Particle Budget | Particle Cull Distance |
|---------|-----------|-----------------|------------------------|
| Low | 30 | 300 | 20 blocks |
| Balanced | 60 | 1000 | 32 blocks |
| Performance | 120 | 2500 | 64 blocks |
| Custom | User-defined | User-defined | User-defined |

## Controls

| Key | Action |
|-----|--------|
| F7 | Toggle performance overlay |

## Compatibility

Lilt checks for installed performance mods and avoids conflicting optimizations:

- Sodium — compatible
- Iris — compatible
- Lithium — compatible
- FerriteCore — compatible
- ImmediatelyFast — compatible
- Entity Culling — compatible
- More Culling — compatible

No mandatory dependency on any of these mods.

## Building

```bash
./gradlew build
```

Output JAR: `build/libs/lilt-0.1.0-dev.1.jar`

## License

MIT

