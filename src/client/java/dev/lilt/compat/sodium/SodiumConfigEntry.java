package dev.lilt.compat.sodium;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SodiumConfigEntry implements ConfigEntryPoint {

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null) {
            return;
        }

        StorageEventHandler storage = LiltClient::onConfigUpdated;

        builder.registerOwnModOptions()
            .setName("Lilt")
            .setVersion("0.1.0-dev.2")
            .addPage(builder.createOptionPage()
                .setName(Component.literal("Lilt"))
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Adaptive Pacing & Tuning"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "adaptive"))
                        .setName(Component.literal("Adaptive Workload Tuning"))
                        .setTooltip(Component.literal("Dynamically scales particle workload during severe frametime drops to preserve smooth frame pacing."))
                        .setDefaultValue(config.isAdaptiveEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setAdaptiveEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isAdaptiveEnabled)
                    )
                )
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Rendering & Animation Optimizations"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "animation_lod"))
                        .setName(Component.literal("Entity Animation LOD"))
                        .setTooltip(Component.literal("Throttles distant entity model animations based on camera distance to eliminate massive FPS drops in villages and mob farms."))
                        .setDefaultValue(config.isAnimationLodEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setAnimationLodEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isAnimationLodEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "texture_culling"))
                        .setName(Component.literal("Texture Animation Optimization"))
                        .setTooltip(Component.literal("Throttles animated texture buffer uploads across PCIe bus during frametime spikes and while menus are active."))
                        .setDefaultValue(config.isTextureCullingEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setTextureCullingEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isTextureCullingEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "particle_culling"))
                        .setName(Component.literal("Particle Culling"))
                        .setTooltip(Component.literal("Skips rendering of distant particles outside culling radius. Near-camera particles are always preserved."))
                        .setDefaultValue(config.isParticleOptimizationEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setParticleOptimizationEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isParticleOptimizationEnabled)
                    )
                    .addOption(builder.createIntegerOption(Identifier.fromNamespaceAndPath("lilt", "particle_budget"))
                        .setName(Component.literal("Particle Budget"))
                        .setTooltip(Component.literal("Maximum concurrent particles allowed across the world."))
                        .setRange(100, 3000, 100)
                        .setValueFormatter(v -> Component.literal(String.valueOf(v)))
                        .setDefaultValue(config.getParticleBudget())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setParticleBudget(val);
                            LiltClient.onConfigUpdated();
                        }, config::getParticleBudget)
                    )
                    .addOption(builder.createIntegerOption(Identifier.fromNamespaceAndPath("lilt", "cull_distance"))
                        .setName(Component.literal("Cull Distance"))
                        .setTooltip(Component.literal("Radius in blocks beyond which particles are not spawned."))
                        .setRange(16, 64, 4)
                        .setValueFormatter(v -> Component.literal(v + " blocks"))
                        .setDefaultValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())))
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setParticleDistanceCullSq(val * val);
                            LiltClient.onConfigUpdated();
                        }, () -> (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())))
                    )
                )
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Engine & Memory Stability"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "tick_burst_protection"))
                        .setName(Component.literal("Tick Burst Limiter"))
                        .setTooltip(Component.literal("Prevents singleplayer internal server tick catch-up bursts during chunk loading from starving the client render thread."))
                        .setDefaultValue(config.isTickBurstProtectionEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setTickBurstProtectionEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isTickBurstProtectionEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "gc_guard"))
                        .setName(Component.literal("Proactive Memory Guard"))
                        .setTooltip(Component.literal("Proactively evacuates Java heap when paused if memory pressure is high, preventing in-game Major GC freezes."))
                        .setDefaultValue(config.isGcGuardEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setGcGuardEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isGcGuardEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "hud_overlay"))
                        .setName(Component.literal("HUD Overlay (F7)"))
                        .setTooltip(Component.literal("Displays live performance telemetry: FPS, frame pacing, tick times, GC pauses, and bottlenecks."))
                        .setDefaultValue(config.isOverlayEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setOverlayEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isOverlayEnabled)
                    )
                )
            );
    }
}