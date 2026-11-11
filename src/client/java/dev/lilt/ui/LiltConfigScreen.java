package dev.lilt.ui;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LiltConfigScreen extends Screen {

    private final Screen lastScreen;
    private final LiltConfig config;

    private static final List<String> PROFILES = List.of("balanced", "low", "performance", "custom");
    private static final List<Integer> TARGET_FPS_OPTIONS = List.of(30, 60, 75, 120, 144, 240, 260);
    private static final List<Integer> PARTICLE_BUDGET_OPTIONS = List.of(100, 300, 500, 1000, 1500, 2500, 5000);
    private static final List<Integer> CULL_DISTANCE_OPTIONS = List.of(16, 20, 32, 48, 64);

    public LiltConfigScreen(Screen lastScreen, LiltConfig config) {
        super(Component.literal("Lilt Settings"));
        this.lastScreen = lastScreen;
        this.config = config;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 155;
        int rightX = this.width / 2 + 5;
        int buttonWidth = 150;
        int buttonHeight = 20;

        String currentProfile = config.getActiveProfile().toLowerCase();
        if (!PROFILES.contains(currentProfile)) {
            currentProfile = "balanced";
        }

        CycleButton<String> profileBtn = CycleButton.builder(
            val -> Component.literal(val.substring(0, 1).toUpperCase() + val.substring(1)),
            currentProfile
        ).withValues(PROFILES).create(
            leftX, 40, buttonWidth, buttonHeight,
            Component.literal("Profile"),
            (btn, val) -> {
                config.setActiveProfile(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        profileBtn.setTooltip(Tooltip.create(Component.literal(
            "Balanced: Recommended default baseline.\nLow: Maximum FPS savings on budget hardware.\nPerformance: High fidelity for strong GPUs.\nCustom: Preserves your manual adjustments."
        )));
        this.addRenderableWidget(profileBtn);

        int currentFps = config.getTargetFps();
        int targetIndex = TARGET_FPS_OPTIONS.contains(currentFps) ? currentFps : 60;
        CycleButton<Integer> fpsBtn = CycleButton.builder(
            val -> Component.literal(val >= 260 ? "Unlimited" : val + " FPS"),
            targetIndex
        ).withValues(TARGET_FPS_OPTIONS).create(
            rightX, 40, buttonWidth, buttonHeight,
            Component.literal("Target FPS"),
            (btn, val) -> {
                config.setTargetFps(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        fpsBtn.setTooltip(Tooltip.create(Component.literal(
            "Framerate threshold used by Adaptive Tuning to detect frame drops."
        )));
        this.addRenderableWidget(fpsBtn);

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            leftX, 64, buttonWidth, buttonHeight,
            Component.literal("Adaptive Tuning"),
            (btn, val) -> {
                config.setAdaptiveEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        adaptiveBtn.setTooltip(Tooltip.create(Component.literal(
            "Dynamically scales particle workload down during severe frame drops to maintain smooth frame pacing."
        )));
        this.addRenderableWidget(adaptiveBtn);

        CycleButton<Boolean> particleBtn = CycleButton.onOffBuilder(config.isParticleOptimizationEnabled()).create(
            rightX, 64, buttonWidth, buttonHeight,
            Component.literal("Particle Culling"),
            (btn, val) -> {
                config.setParticleOptimizationEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        particleBtn.setTooltip(Tooltip.create(Component.literal(
            "Skips rendering of distant particles outside culling range. Near-camera particles are always preserved."
        )));
        this.addRenderableWidget(particleBtn);

        CycleButton<Boolean> overlayBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            leftX, 88, buttonWidth, buttonHeight,
            Component.literal("HUD Overlay (F7)"),
            (btn, val) -> {
                config.setOverlayEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        overlayBtn.setTooltip(Tooltip.create(Component.literal(
            "Displays live telemetry overlay: FPS, pacing, tick time, GC pauses, and bottleneck diagnosis."
        )));
        this.addRenderableWidget(overlayBtn);

        int currentBudget = config.getParticleBudget();
        int budgetVal = PARTICLE_BUDGET_OPTIONS.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budgetVal
        ).withValues(PARTICLE_BUDGET_OPTIONS).create(
            rightX, 88, buttonWidth, buttonHeight,
            Component.literal("Particle Budget"),
            (btn, val) -> {
                config.setParticleBudget(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        budgetBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum active particles allowed. Lower values eliminate frame drops during intense combat or explosions."
        )));
        this.addRenderableWidget(budgetBtn);

        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int distVal = CULL_DISTANCE_OPTIONS.contains(currentDist) ? currentDist : 32;
        CycleButton<Integer> distBtn = CycleButton.builder(
            val -> Component.literal(val + " blocks"),
            distVal
        ).withValues(CULL_DISTANCE_OPTIONS).create(
            leftX, 112, buttonWidth, buttonHeight,
            Component.literal("Cull Distance"),
            (btn, val) -> {
                config.setParticleDistanceCullSq(val * val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        distBtn.setTooltip(Tooltip.create(Component.literal(
            "Radius in blocks beyond which particles are not spawned."
        )));
        this.addRenderableWidget(distBtn);

        Button resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("balanced");
            LiltClient.onConfigUpdated();
            this.rebuildWidgets();
        }).bounds(rightX, 112, buttonWidth, buttonHeight).build();
        resetBtn.setTooltip(Tooltip.create(Component.literal("Restores all settings back to the default Balanced profile.")));
        this.addRenderableWidget(resetBtn);

        Button doneBtn = Button.builder(Component.literal("Done"), btn -> {
            LiltClient.onConfigUpdated();
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 30, 200, 20).build();
        this.addRenderableWidget(doneBtn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.lastScreen);
        }
    }
}
