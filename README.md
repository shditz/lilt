<p align="center">
  <img src="https://raw.githubusercontent.com/shditz/lilt/main/assets/banner.png" alt="Lilt Banner" width="100%">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/shditz/lilt/main/assets/icon.png" alt="Lilt Icon" width="96" height="96"><br>
  <strong>Client-side performance mod focusing on frame pacing and micro-stutters.</strong><br>
  <sub>Fabric • Minecraft 26.3, 26.2, 26.1.2, 1.21.11, 1.21.4, 1.21</sub>
</p>

---

## What is Lilt?

**Lilt** is a client-side Fabric mod designed to eliminate micro-stutters and frametime spikes.

Mods like **Sodium** do an amazing job optimizing terrain and chunk rendering, but your game can still stutter in common situations:
- Walking into a dense mob farm or villager trading hall.
- Exploring new chunks in singleplayer while flying with an Elytra.
- Hundreds of mobs producing identical sounds at the same second.
- Huge explosions or boss fights filling the screen with particles.
- Java garbage collection pauses happening mid-fight.

Lilt fixes these exact bottlenecks on the client render thread without changing game mechanics or server behavior.

---

## Features

- **Entity Animation LOD:** Slows down limb animation updates for distant mobs. Mobs near you stay completely smooth, while a pen of 100 cows 30 blocks away won't drag down your frame rate.
- **Mob Farm Sound Limiter:** Stops audio engine overload. Caps duplicate sounds (cows mooing, zombies groaning, pistons firing) playing at the same spot within a fraction of a second.
- **Entity Shadow Culling:** Skips shadow rendering for entities that are far away or standing in pitch-black darkness where shadows aren't visible.
- **Beacon Beam Culling:** Stops rendering beacon and End gateway beams when they are behind your camera or out of view.
- **Tick Catch-Up Limiter:** When singleplayer terrain generation falls behind, Minecraft tries to run catch-up ticks all at once, freezing the camera. Lilt caps consecutive catch-up iterations so your game stays responsive.
- **Adaptive Particle Limiter:** Automatically scales down particle limits during sudden spikes (TNT explosions, dragon breath) to protect your frametime.
- **Texture Upload Throttling:** Reduces PCIe bandwidth pressure from animated blocks (fire, water, modded textures), especially helpful on integrated graphics (iGPUs) and laptops.
- **Pause Menu Memory Cleanup:** Triggers a quick garbage collection pass when you open the pause menu, clearing accumulated memory so you don't get frozen mid-game.
- **Minimal Performance HUD (F7):** Press `F7` to toggle a lightweight, zero-allocation HUD showing current FPS, 1% low FPS, and memory usage.

---

## Built-in Profiles

Lilt can be configured directly from Minecraft's **Video Settings** (works in both vanilla settings and Sodium's menu):

- **Medium (Default):** Balanced for everyday survival. Smooth frame pacing with no noticeable visual difference.
- **Low:** Aggressive optimizations for budget laptops, old PCs, and integrated graphics.
- **High:** Maximum visual distance with safety limits for high-end PCs.
- **Server:** Tuned for crowded multiplayer servers, mega-bases, and large farms.
- **Custom:** Manually tweak every slider and toggle to your liking.

---

## Compatibility

Lilt does not replace Minecraft's rendering engine. It works alongside other performance mods:

- **Sodium:** Fully compatible. Lilt integrates into Sodium's settings page.
- **Lithium:** Fully compatible.
- **FerriteCore:** Fully compatible.
- **ImmediatelyFast:** Fully compatible.
- **EntityCulling:** Fully compatible.
- **Iris Shaders:** Fully compatible.

---

## FAQ

**Is Lilt client-side only?**  
Yes. Lilt only needs to be installed on your client. You can join vanilla, Paper, Purpur, Fabric, or Forge servers without any issues.

**Does this change mob AI or hitboxes?**  
No. Hitboxes, reach, entity physics, and mob behavior remain 100% vanilla. Only client-side visual and audio rendering is optimized.

**Do I need Sodium to use Lilt?**  
No. Sodium is recommended, but optional. Lilt works on completely vanilla Fabric and has its own menu inside standard Video Settings.

---

## License

MIT License. Free and open source.
