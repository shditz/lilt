package dev.lilt.compat.sodium;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
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

        builder.registerOwnModOptions()
            .setName("Lilt")
            .addPage(builder.createOptionPage()
                .setName(Component.literal("Lilt"))
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Adaptive Pacing"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "adaptive"))
                        .setName(Component.literal("Adaptive Tuning"))
                        .setTooltip(Component.literal("Dynamically reduces particle workload during severe frametime drops to maintain smooth frame pacing."))
                        .setDefaultValue(config.isAdaptiveEnabled())
                        .setBinding(val -> {
                            config.setAdaptiveEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isAdaptiveEnabled)
                    )
                    .addOption(builder.createIntegerOption(Identifier.fromNamespaceAndPath("lilt", "target_fps"))
                        .setName(Component.literal("Target Framerate"))
                        .setTooltip(Component.literal("Framerate threshold used by Adaptive Tuning to detect performance drops."))
                        .setRange(30, 240, 10)
                        .setValueFormatter(v -> Component.literal(v + " FPS"))
                        .setDefaultValue(config.getTargetFps())
                        .setBinding(val -> {
                            config.setTargetFps(val);
                            LiltClient.onConfigUpdated();
                        }, config::getTargetFps)
                    )
                )
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Particle Optimizer"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "particle_culling"))
                        .setName(Component.literal("Particle Culling"))
                        .setTooltip(Component.literal("Skips rendering of distant particles outside culling radius. Near-camera particles are always preserved."))
                        .setDefaultValue(config.isParticleOptimizationEnabled())
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
                        .setBinding(val -> {
                            config.setParticleDistanceCullSq(val * val);
                            LiltClient.onConfigUpdated();
                        }, () -> (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())))
                    )
                )
                .addOptionGroup(builder.createOptionGroup()
                    .setName(Component.literal("Diagnostics & HUD"))
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("lilt", "hud_overlay"))
                        .setName(Component.literal("HUD Overlay (F7)"))
                        .setTooltip(Component.literal("Displays live performance telemetry: FPS, frame pacing, tick times, GC pauses, and bottlenecks."))
                        .setDefaultValue(config.isOverlayEnabled())
                        .setBinding(val -> {
                            config.setOverlayEnabled(val);
                            LiltClient.onConfigUpdated();
                        }, config::isOverlayEnabled)
                    )
                )
            );
    }
}
