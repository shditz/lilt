package dev.lilt.mixin;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("unused")
@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {

    protected VideoSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    @Inject(method = "addOptions", at = @At("RETURN"))
    private void lilt$addLiltOptions(CallbackInfo ci) {
        LiltConfig config = LiltClient.getConfig();
        if (config == null) {
            return;
        }

        List<String> profiles = List.of("balanced", "low", "performance", "custom");
        String currentProfile = config.getActiveProfile().toLowerCase();
        if (!profiles.contains(currentProfile)) {
            currentProfile = "balanced";
        }

        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] adaptiveHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] animLodHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] texCullHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] tickGuardHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] gcGuardHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] particleHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] budgetHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] distHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];

        CycleButton<String> profileBtn = CycleButton.<String>builder(
            val -> Component.literal(val.substring(0, 1).toUpperCase() + val.substring(1))
        ).withValues(profiles).withInitialValue(currentProfile).create(
            Component.literal("Lilt Profile"),
            (btn, val) -> {
                config.setActiveProfile(val);
                LiltClient.onConfigUpdated();
                if (adaptiveHolder[0] != null) adaptiveHolder[0].setValue(config.isAdaptiveEnabled());
                if (animLodHolder[0] != null) animLodHolder[0].setValue(config.isAnimationLodEnabled());
                if (texCullHolder[0] != null) texCullHolder[0].setValue(config.isTextureCullingEnabled());
                if (tickGuardHolder[0] != null) tickGuardHolder[0].setValue(config.isTickBurstProtectionEnabled());
                if (gcGuardHolder[0] != null) gcGuardHolder[0].setValue(config.isGcGuardEnabled());
                if (particleHolder[0] != null) particleHolder[0].setValue(config.isParticleOptimizationEnabled());
                if (budgetHolder[0] != null) budgetHolder[0].setValue(config.getParticleBudget());
                if (distHolder[0] != null) distHolder[0].setValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())));
            }
        );
        profileBtn.setTooltip(Tooltip.create(Component.literal(
            "Balanced: Recommended default baseline.\nLow: Maximum FPS savings on budget hardware.\nPerformance: High fidelity for strong GPUs.\nCustom: Preserves your manual adjustments."
        )));

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            Component.literal("Adaptive Tuning"),
            (btn, val) -> {
                config.setAdaptiveEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        adaptiveBtn.setTooltip(Tooltip.create(Component.literal(
            "Dynamically scales particle workload down during severe frame drops to maintain smooth frame pacing."
        )));
        adaptiveHolder[0] = adaptiveBtn;

        this.list.addSmall(profileBtn, adaptiveBtn);

        CycleButton<Boolean> animLodBtn = CycleButton.onOffBuilder(config.isAnimationLodEnabled()).create(
            Component.literal("Animation LOD"),
            (btn, val) -> {
                config.setAnimationLodEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        animLodBtn.setTooltip(Tooltip.create(Component.literal(
            "Throttles distant entity model animations based on distance. Greatly improves FPS in villages and farms."
        )));
        animLodHolder[0] = animLodBtn;

        CycleButton<Boolean> texCullBtn = CycleButton.onOffBuilder(config.isTextureCullingEnabled()).create(
            Component.literal("Texture Optimization"),
            (btn, val) -> {
                config.setTextureCullingEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        texCullBtn.setTooltip(Tooltip.create(Component.literal(
            "Throttles animated texture buffer uploads across PCIe during frametime spikes and while paused."
        )));
        texCullHolder[0] = texCullBtn;

        this.list.addSmall(animLodBtn, texCullBtn);

        CycleButton<Boolean> tickGuardBtn = CycleButton.onOffBuilder(config.isTickBurstProtectionEnabled()).create(
            Component.literal("Tick Burst Limiter"),
            (btn, val) -> {
                config.setTickBurstProtectionEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        tickGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Prevents singleplayer internal server catch-up tick bursts from locking the CPU and freezing rendering."
        )));
        tickGuardHolder[0] = tickGuardBtn;

        CycleButton<Boolean> gcGuardBtn = CycleButton.onOffBuilder(config.isGcGuardEnabled()).create(
            Component.literal("Memory Guard"),
            (btn, val) -> {
                config.setGcGuardEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        gcGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Proactively triggers garbage collection while paused if memory pressure is high to prevent in-game freezes."
        )));
        gcGuardHolder[0] = gcGuardBtn;

        this.list.addSmall(tickGuardBtn, gcGuardBtn);

        CycleButton<Boolean> particleBtn = CycleButton.onOffBuilder(config.isParticleOptimizationEnabled()).create(
            Component.literal("Particle Culling"),
            (btn, val) -> {
                config.setParticleOptimizationEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        particleBtn.setTooltip(Tooltip.create(Component.literal(
            "Skips rendering of distant particles outside culling range. Near-camera particles are always preserved."
        )));
        particleHolder[0] = particleBtn;

        List<Integer> budgetOptions = List.of(100, 300, 500, 1000, 1500, 2500);
        int currentBudget = config.getParticleBudget();
        int budget = budgetOptions.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.<Integer>builder(
            val -> Component.literal(String.valueOf(val))
        ).withValues(budgetOptions).withInitialValue(budget).create(
            Component.literal("Particle Budget"),
            (btn, val) -> {
                config.setParticleBudget(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        budgetBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum active particles allowed. Lower values eliminate frame drops during intense combat or explosions."
        )));
        budgetHolder[0] = budgetBtn;

        this.list.addSmall(particleBtn, budgetBtn);

        List<Integer> distOptions = List.of(16, 20, 32, 48, 64);
        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int dist = distOptions.contains(currentDist) ? currentDist : 32;
        CycleButton<Integer> distBtn = CycleButton.<Integer>builder(
            val -> Component.literal(val + " blocks")
        ).withValues(distOptions).withInitialValue(dist).create(
            Component.literal("Cull Distance"),
            (btn, val) -> {
                config.setParticleDistanceCullSq(val * val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        distBtn.setTooltip(Tooltip.create(Component.literal(
            "Radius in blocks beyond which particles are not spawned."
        )));
        distHolder[0] = distBtn;

        CycleButton<Boolean> hudBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            Component.literal("HUD Overlay (F7)"),
            (btn, val) -> {
                config.setOverlayEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        hudBtn.setTooltip(Tooltip.create(Component.literal(
            "Displays live telemetry overlay: FPS, pacing, tick time, GC pauses, and bottleneck diagnosis."
        )));

        this.list.addSmall(distBtn, hudBtn);

        Button resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("balanced");
            LiltClient.onConfigUpdated();
            profileBtn.setValue("balanced");
            if (adaptiveHolder[0] != null) adaptiveHolder[0].setValue(config.isAdaptiveEnabled());
            if (animLodHolder[0] != null) animLodHolder[0].setValue(config.isAnimationLodEnabled());
            if (texCullHolder[0] != null) texCullHolder[0].setValue(config.isTextureCullingEnabled());
            if (tickGuardHolder[0] != null) tickGuardHolder[0].setValue(config.isTickBurstProtectionEnabled());
            if (gcGuardHolder[0] != null) gcGuardHolder[0].setValue(config.isGcGuardEnabled());
            if (particleHolder[0] != null) particleHolder[0].setValue(config.isParticleOptimizationEnabled());
            if (budgetHolder[0] != null) budgetHolder[0].setValue(config.getParticleBudget());
            if (distHolder[0] != null) distHolder[0].setValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())));
        }).build();
        resetBtn.setTooltip(Tooltip.create(Component.literal(
            "Restores all Lilt settings back to the default Balanced profile."
        )));

        this.list.addSmall(resetBtn, null);
    }
}