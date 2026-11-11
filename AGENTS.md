# Lilt — Agent Rules

This file contains permanent project rules for AI agents working on Lilt.

## Identity

- **Name:** Lilt
- **Mod ID:** `lilt`
- **Package:** `dev.lilt`
- **License:** MIT
- **Target:** Minecraft Java Edition 26.3 (Fabric)
- **Java:** 25

## Technical Restrictions

- Java is the only implementation language.
- No Rust, JNI, Project Panama/FFM, or native libraries.
- No custom Vulkan backends, custom graphics APIs, or custom rendering engines.
- Do not replace Sodium or recreate its rendering engine.
- Keep Minecraft's existing rendering architecture.
- Avoid unnecessary third-party dependencies.

## Architecture Rules

- Use Fabric API hooks when available. Use Mixin only when necessary.
- Avoid `@Overwrite` and invasive mixin modifications.
- Do not create unnecessary abstractions, interfaces, managers, or frameworks.
- Keep code modular: core, config, hardware, monitor, diagnostics, adaptive, optimization, compat, ui, mixin.
- This is a client-side mod. It must not require server installation.

## Safety Rules

- Never silently override the player's Minecraft graphics settings.
- Never automatically change render distance, simulation distance, VSync, or FPS limits.
- The adaptive controller may only modify features controlled by Lilt.
- Never make aggressive adjustments based on a single slow frame.
- Do not perform expensive file operations on the render thread.
- GPU queries must be performed on the render thread, not off-thread.
- GPU timing must be asynchronous when supported. Never introduce blocking GPU readbacks for statistics.
- Do not report unsupported measurements as real values.

## Compatibility

- Check for installed mods using Fabric Loader.
- No mandatory dependency on optional performance mods.
- No direct references to optional mod classes unless properly isolated.
- Disable overlapping optimizations when appropriate.

## Quality

- Verify actual method names, APIs, and Mixin targets against the Minecraft 26.3 source.
- Do not invent classes, methods, or dependencies.
- Avoid unnecessary allocations in hot paths.
- Handle world changes and resource reloads safely.
- Do not leave unfinished features disguised as completed functionality.
- Do not claim performance improvements without benchmark evidence.
- Do not fabricate test results.

## Language

- All code, documentation, comments, config labels, and UI text in English.
- Communication with the project owner in Bahasa Indonesia.
