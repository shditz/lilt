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

        this.list.addHeader(Component.literal("Lilt"));

        List<String> profiles = List.of("balanced", "low", "performance", "custom");
        String currentProfile = config.getActiveProfile().toLowerCase();
        if (!profiles.contains(currentProfile)) {
            currentProfile = "balanced";
        }

        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] fpsHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] adaptiveHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] particleHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] budgetHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] distHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];

        CycleButton<String> profileBtn = CycleButton.builder(
            val -> Component.literal(val.substring(0, 1).toUpperCase() + val.substring(1)),
            currentProfile
        ).withValues(profiles).create(
            Component.literal("Lilt Profile"),
            (btn, val) -> {
                config.setActiveProfile(val);
                LiltClient.onConfigUpdated();
                if (fpsHolder[0] != null) fpsHolder[0].setValue(config.getTargetFps());
                if (adaptiveHolder[0] != null) adaptiveHolder[0].setValue(config.isAdaptiveEnabled());
                if (particleHolder[0] != null) particleHolder[0].setValue(config.isParticleOptimizationEnabled());
                if (budgetHolder[0] != null) budgetHolder[0].setValue(config.getParticleBudget());
                if (distHolder[0] != null) distHolder[0].setValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())));
            }
        );
        profileBtn.setTooltip(Tooltip.create(Component.literal(
            "Balanced: Recommended default baseline.\nLow: Maximum FPS savings on budget hardware.\nPerformance: High fidelity for strong GPUs.\nCustom: Preserves your manual adjustments."
        )));

        List<Integer> fpsOptions = List.of(30, 60, 75, 120, 144, 240, 260);
        int currentFps = config.getTargetFps();
        int targetFps = fpsOptions.contains(currentFps) ? currentFps : 60;
        CycleButton<Integer> fpsBtn = CycleButton.builder(
            val -> Component.literal(val >= 260 ? "Unlimited" : val + " FPS"),
            targetFps
        ).withValues(fpsOptions).create(
            Component.literal("Target FPS"),
            (btn, val) -> {
                config.setTargetFps(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        fpsBtn.setTooltip(Tooltip.create(Component.literal(
            "Framerate threshold used by Adaptive Tuning to detect frame drops."
        )));
        fpsHolder[0] = fpsBtn;

        this.list.addSmall(profileBtn, fpsBtn);

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

        this.list.addSmall(adaptiveBtn, particleBtn);

        List<Integer> budgetOptions = List.of(100, 300, 500, 1000, 1500, 2500);
        int currentBudget = config.getParticleBudget();
        int budget = budgetOptions.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budget
        ).withValues(budgetOptions).create(
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

        List<Integer> distOptions = List.of(16, 20, 32, 48, 64);
        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int dist = distOptions.contains(currentDist) ? currentDist : 32;
        CycleButton<Integer> distBtn = CycleButton.builder(
            val -> Component.literal(val + " blocks"),
            dist
        ).withValues(distOptions).create(
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

        this.list.addSmall(budgetBtn, distBtn);

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

        Button resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("balanced");
            LiltClient.onConfigUpdated();
            profileBtn.setValue("balanced");
            if (fpsHolder[0] != null) fpsHolder[0].setValue(config.getTargetFps());
            if (adaptiveHolder[0] != null) adaptiveHolder[0].setValue(config.isAdaptiveEnabled());
            if (particleHolder[0] != null) particleHolder[0].setValue(config.isParticleOptimizationEnabled());
            if (budgetHolder[0] != null) budgetHolder[0].setValue(config.getParticleBudget());
            if (distHolder[0] != null) distHolder[0].setValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())));
        }).build();
        resetBtn.setTooltip(Tooltip.create(Component.literal(
            "Restores all Lilt settings back to the default Balanced profile."
        )));

        this.list.addSmall(hudBtn, resetBtn);
    }
}
