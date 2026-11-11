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
│   └── PerformanceMonitor — Frame timing, FPS, GC tracking, tick execution duration
├── diagnostics
│   └── PerformanceDiagnostics — Pressure classification
├── adaptive
│   └── AdaptiveController — Automatic tuning with hysteresis, multi-step adjustments, diagnostic checks
├── optimization
│   └── particle
│       └── ParticleOptimizer — Budget and distance culling
├── compat
│   └── ModCompat        — Fabric Loader mod detection
├── ui
│   ├── OverlayRenderer  — Cached HUD rendering
│   └── KeyBindings      — Key registration
└── mixin
    ├── MinecraftMixin   — Render loop frame interval and tick duration hooks
    └── ParticleEngineMixin — Particle lifecycle & budget integration
```

## Initialization Order

1. `LiltMod.onInitialize()` — common init
2. `LiltClient.onInitializeClient()` — client init:
   - Load config
   - Detect hardware baseline
   - Create performance monitor
   - Create diagnostics engine
   - Detect installed mods
   - Create particle optimizer
   - Create adaptive controller
   - Create overlay renderer
   - Register HUD element
   - Register keybindings
   - Register tick callbacks
3. First in-game tick — query GPU device info safely via RenderPearl

## Data Flow

```
PerformanceMonitor (frame intervals, ticks, GC)
        │
        ▼
PerformanceDiagnostics (evidence-based pressure classification)
        │
        ▼
AdaptiveController (checks pressure relevance, adjusts particle budget)
        │
        ▼
ParticleOptimizer (budget & distance decisions)
        │
        ▼
ParticleEngineMixin (budget enforcement & lifecycle sync)
```

## Mixin Strategy

- `@Inject` only — zero `@Overwrite` or invasive redirects
- `MinecraftMixin` — non-invasive hook on `renderFrame` to measure frame presentation intervals, and on `tick` to measure client tick execution duration and inter-tick interval
- `ParticleEngineMixin` — intercepts `createParticle` for budget and distance culling, synchronizes active counts on `tick`, and resets on world unload

## Configuration

JSON-based, stored at `.minecraft/config/lilt.json`. Schema version tracks breaking changes. Invalid configs are backed up and replaced with defaults.

## Compatibility

Uses `FabricLoader.isModLoaded()` for detection. No class-level references to optional mods. Overlapping features can be conditionally disabled.
