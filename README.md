<p align="center">
  <img src="https://raw.githubusercontent.com/shditz/lilt/main/assets/banner.png" alt="Lilt Banner" width="100%">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/shditz/lilt/main/assets/icon.png" alt="Lilt Icon" width="96" height="96"><br>
  <strong>Client-side performance stabilizer and frame-pacing optimizer for Minecraft Fabric.</strong>
</p>

---

## What is Lilt?

**Lilt** focuses on the frametime spikes, micro-stutters, and sudden hitching that chunk-rendering mods do not address.

While mods like **Sodium** optimize terrain and chunk meshing, games still stutter when entering crowded mob farms, generating new chunks on singleplayer, uploading animated block textures, or triggering Java garbage collection pauses. Lilt tackles these bottlenecks directly on the client render thread without altering game mechanics or breaking visual immersion.

---

## Before & After Lilt

| Scenario | Without Lilt (Vanilla / Sodium Only) | With Lilt (+ Sodium) |
| :--- | :--- | :--- |
| **Crowded Villages & Mob Farms**<br>*(50+ entities active)* | Full skeletal/limb animations calculated every frame for every entity regardless of distance. 1% low FPS plummets, causing stuttery camera rotation. | Distant mob animations update at stepped intervals via distance LOD. Near-player entities remain buttery smooth, and 1% low FPS stays locked. |
| **Singleplayer Chunk Generation**<br>*(Fast Elytra / Boat travel)* | Internal server thread falls behind and floods the tick loop with catch-up bursts, causing the client render thread to stutter and freeze. | Tick burst limiter caps catch-up iterations per frame. Camera movement and rendering remain smooth even during intensive terrain gen. |
| **Intense Explosions & Combat**<br>*(TNT, Wither, Dragon breath)* | Uncapped particle bursts flood the render queue, causing massive frametime spikes ($P_{99} > 60\text{ ms}$). | Adaptive particle engine dynamically culls distant particles and caps active budget during severe spikes, maintaining fluid frame pacing. |
| **Animated Blocks & Texture Uploads**<br>*(Water, fire, modded animated blocks)* | Continuous PCIe buffer copies saturate memory bandwidth on integrated GPUs (iGPUs) and budget graphics cards. | Staggers and throttles animated texture uploads across PCIe during frametime spikes and while paused, saving crucial GPU memory bus headroom. |
| **Long Play Sessions**<br>*(Memory accumulation & GC)* | Random 100–500ms "stop-the-world" Java garbage collection freezes in the middle of active gameplay or combat. | Proactive memory guard triggers safe, lightweight GC sweeps during pause and menu screens, eliminating mid-fight freezes. |

---

## Core Optimizations

### 1. Entity Animation LOD (Level of Detail)
- Throttles bone and limb animation updates for distant entities based on camera distance and current frametime pressure.
- Prevents mob farms, villages, and crowded pastures from tanking your 1% low FPS.
- Entities close to the crosshair and player retain full 60+ FPS animation fidelity.

### 2. Singleplayer Tick Burst Limiter
- In singleplayer worlds, chunk loading and internal server catch-up routines can flood the tick loop, causing the client render thread to freeze.
- Lilt limits consecutive catch-up tick bursts per frame, ensuring smooth frame pacing even during aggressive terrain generation.

### 3. Texture Bus Throttling
- Constantly uploading animated block textures across PCIe can saturate memory bandwidth on integrated GPUs (iGPUs) and budget graphics cards.
- Lilt dynamically throttles redundant animated texture updates when frametime variance exceeds safety thresholds or when the game is paused.

### 4. Proactive Garbage Collection Guard
- Monitors heap allocation pressure and triggers lightweight, incremental GC sweeps during pause screens and menu interactions.
- Cleans up dead memory before intense gameplay situations, eliminating stop-the-world GC stutters during combat.

### 5. Adaptive Particle Throttling
- Dynamically scales particle spawn limits and applies distance culling during explosions, dragon breath, and wither battles based on real-time P99/P50 frametime ratios.

### 6. Zero-Allocation Diagnostics HUD (`F7`)
- Provides clean, lightweight diagnostic telemetry: current FPS, 1% low FPS, tick duration, memory pressure, and GPU hardware vendor.
- Implemented with zero heap allocations per frame to avoid introducing any measurement-induced stutter.

---

## In-Game Profiles

Lilt integrates directly into Minecraft's native **Video Settings** as well as Sodium's settings menu:

- **Balanced (Default)**: Recommended preset. Delivers consistent frame pacing with zero perceptible visual impact.
- **Low**: Aggressive culling and animation throttling tailored for low-spec laptops and integrated graphics.
- **Performance**: High visual fidelity with protective safety caps for mid-to-high-end hardware.
- **Custom**: Full granular control over individual toggles, particle budgets, and culling distances.

---

## Mod Compatibility

Lilt does not replace Minecraft's rendering pipeline or overwrite vanilla shaders. It is fully compatible with:

| Mod | Compatibility | Note |
| :--- | :---: | :--- |
| **Sodium** | Supported | Complementary; Lilt optimizes entity and client workloads outside Sodium's chunk engine. |
| **Lithium** | Supported | Fully compatible with all server-side physics and logic optimizations. |
| **FerriteCore** | Supported | Fully compatible; both mods reduce memory overhead from different angles. |
| **ImmediatelyFast** | Supported | Fully compatible with UI and font rendering batching. |
| **EntityCulling** | Supported | Complementary; EntityCulling skips off-screen entities, Lilt optimizes on-screen distant animations. |
| **Iris Shaders** | Supported | Compatible with shader pack pipelines. |

---

## Frequently Asked Questions

**Is Lilt required on the server?**  
No. Lilt is 100% client-side. It does not send custom network packets and works on standard vanilla, Fabric, Paper, or Purpur servers.

**Does Lilt alter game mechanics or hitboxes?**  
No. Animation LOD and particle limits affect only visual updates on the client. Entity hitboxes, AI pathfinding, and combat mechanics remain identical to vanilla.

---

## License

Lilt is open-source software licensed under the [MIT License](LICENSE).
