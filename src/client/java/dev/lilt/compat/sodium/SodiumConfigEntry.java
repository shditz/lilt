package dev.lilt.compat.sodium;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unused")
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
            .setVersion("1.0.0")
            .addPage(builder.createOptionPage()
                .setName(Component.literal("Lilt"))
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Dynamic Workload"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "adaptive"))
                        .setName(Component.literal("Dynamic Particle Scaling"))
                        .setTooltip(Component.literal("Temporarily reduces particle counts during lag spikes to help keep framerates steady."))
                        .setDefaultValue(config.isAdaptiveEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setAdaptiveEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isAdaptiveEnabled)
                    )
                )
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Rendering & Animations"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "animation_lod"))
                        .setName(Component.literal("Smart Entity Animations"))
                        .setTooltip(Component.literal("Adjusts animation update rates for distant and off-screen mobs and players based on your camera view. Saves CPU in crowded areas."))
                        .setDefaultValue(config.isAnimationLodEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setAnimationLodEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isAnimationLodEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "texture_culling"))
                        .setName(Component.literal("Optimized Texture Updates"))
                        .setTooltip(Component.literal("Pauses animated textures like water, lava, and fire while the game is paused or during severe lag."))
                        .setDefaultValue(config.isTextureCullingEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setTextureCullingEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isTextureCullingEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "particle_culling"))
                        .setName(Component.literal("Particle Culling"))
                        .setTooltip(Component.literal("Hides particles that are outside your render distance."))
                        .setDefaultValue(config.isParticleOptimizationEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setParticleOptimizationEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isParticleOptimizationEnabled)
                    )
                    .addOption(builder.createIntegerOption(Identifier.fromNamespaceAndPath("lilt", "particle_budget"))
                        .setName(Component.literal("Particle Limit"))
                        .setTooltip(Component.literal("Maximum number of active particles allowed at one time."))
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
                        .setName(Component.literal("Particle Distance"))
                        .setTooltip(Component.literal("Maximum distance from the camera to render particles."))
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
                    .setName(Component.literal("Memory & Engine"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "tick_burst_protection"))
                        .setName(Component.literal("Server Catch-Up Limiter"))
                        .setTooltip(Component.literal("Prevents singleplayer server tick bursts from freezing the client render thread."))
                        .setDefaultValue(config.isTickBurstProtectionEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setTickBurstProtectionEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isTickBurstProtectionEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "gc_guard"))
                        .setName(Component.literal("Memory Cleanup on Pause"))
                        .setTooltip(Component.literal("Reclaims unused Java memory when the pause menu is opened if memory usage is high."))
                        .setDefaultValue(config.isGcGuardEnabled())
                        .setStorageHandler(storage)
                        .setBinding(val -> {
                            config.setGcGuardEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isGcGuardEnabled)
                    )
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "hud_overlay"))
                        .setName(Component.literal("Performance Overlay (F7)"))
                        .setTooltip(Component.literal("Shows an on-screen overlay with FPS, frametimes, and memory status."))
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
