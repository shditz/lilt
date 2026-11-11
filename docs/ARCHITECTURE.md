# Architecture

Lilt follows a modular architecture with clear separation of concerns.

## Package Structure

```
dev.lilt
├── LiltMod              — Common mod entry point
├── client
│   └── LiltClient       — Client entry point, subsystem initialization
├── config
│   └── LiltConfig       — JSON config, profiles, schema versioning
├── hardware
│   └── HardwareInfo     — CPU/GPU/JVM detection
├── monitor
│   └── PerformanceMonitor — Frame timing, FPS, GC tracking
├── diagnostics
│   └── PerformanceDiagnostics — Pressure classification
├── adaptive
│   └── AdaptiveController — Automatic tuning with hysteresis
├── optimization
│   └── particle
│       └── ParticleOptimizer — Budget and distance culling
├── compat
│   └── ModCompat        — Fabric Loader mod detection
├── ui
│   ├── OverlayRenderer  — HUD rendering
│   └── KeyBindings      — Key registration
└── mixin
    ├── MinecraftClientMixin — Render hook
    └── ParticleManagerMixin — Particle budget integration
```

## Initialization Order

1. `LiltMod.onInitialize()` — common init (logging only)
2. `LiltClient.onInitializeClient()` — client init:
   - Load config
   - Detect hardware (CPU/JVM)
   - Create performance monitor
   - Create diagnostics
   - Detect installed mods
   - Create particle optimizer
   - Create adaptive controller
   - Create overlay renderer
   - Register keybindings
   - Register tick and HUD callbacks
3. First in-game tick — query GPU info (requires GL context)

## Data Flow

```
PerformanceMonitor (frame times, GC)
        │
        ▼
PerformanceDiagnostics (pressure classification)
        │
        ▼
AdaptiveController (adjusts particle budget)
        │
        ▼
ParticleOptimizer (budget/distance decisions)
        │
        ▼
ParticleManagerMixin (intercepts particle additions)
```

## Mixin Strategy

- `@Inject` only — no `@Overwrite` or `@Redirect`
- `MinecraftClientMixin` — hook for render loop (future instrumentation)
- `ParticleManagerMixin` — intercepts `addParticle` and `tick` for budget management

## Configuration

JSON-based, stored at `.minecraft/config/lilt.json`. Schema version tracks breaking changes. Invalid configs are backed up and replaced with defaults.

## Compatibility

Uses `FabricLoader.isModLoaded()` for detection. No class-level references to optional mods. Overlapping features can be conditionally disabled.

