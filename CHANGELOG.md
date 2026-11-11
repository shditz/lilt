# Changelog

## 0.1.0-dev.2

Refinements, diagnostics correctness, and overhead reduction.

### Fixed
- Client tick measurement: separated actual execution duration from inter-tick scheduling intervals.
- Adaptive controller: enabled multi-step incremental adjustments across cooldowns with minimum budget enforcement.
- Adaptive controller: integrated user framerate cap to avoid false degradation under intentional FPS limits.
- Adaptive controller: added diagnostic relevance check to suppress particle budget reductions during CPU tick or GC memory pressure.
- Performance overlay: added 250ms text and color caching to eliminate per-frame allocations and `String.format` overhead.
- Mixin mappings: configured `remap = false` on `MinecraftMixin` and `ParticleEngineMixin` for Minecraft 26.3 Mojang mappings environment.
- Mod metadata: pinned Minecraft dependency to `=26.3` and removed reference to missing icon asset.

## 0.1.0-dev.1

Initial development release.

### Added
- Performance monitoring: FPS, frame times (P50/P95/P99), 1% low FPS, GC activity, tick timing
- Performance overlay with F7 toggle
- Hardware detection (CPU, GPU, JVM, OpenGL/Vulkan)
- Performance diagnostics: CPU-bound, GPU-bound, memory pressure, mixed classification
- Adaptive performance controller with hysteresis and cooldowns
- Particle optimization with budget management and distance culling
- Mod compatibility detection (Sodium, Iris, Lithium, FerriteCore, ImmediatelyFast, Entity Culling, More Culling)
- JSON configuration with profiles (Low, Balanced, Performance, Custom)
- Schema versioning and safe config recovery
