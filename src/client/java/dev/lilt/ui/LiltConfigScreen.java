package dev.lilt.ui;

import dev.lilt.client.LiltClient;
import dev.lilt.config.LiltConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
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
        super(Component.literal("Lilt Configuration"));
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
            leftX, 48, buttonWidth, buttonHeight,
            Component.literal("Profile"),
            (btn, val) -> {
                config.setActiveProfile(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        this.addRenderableWidget(profileBtn);

        int currentFps = config.getTargetFps();
        int targetIndex = TARGET_FPS_OPTIONS.contains(currentFps) ? currentFps : 60;
        CycleButton<Integer> fpsBtn = CycleButton.builder(
            val -> Component.literal(val >= 260 ? "Unlimited" : val + " FPS"),
            targetIndex
        ).withValues(TARGET_FPS_OPTIONS).create(
            rightX, 48, buttonWidth, buttonHeight,
            Component.literal("Target FPS"),
            (btn, val) -> {
                config.setTargetFps(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        this.addRenderableWidget(fpsBtn);

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            leftX, 74, buttonWidth, buttonHeight,
            Component.literal("Adaptive Tuning"),
            (btn, val) -> {
                config.setAdaptiveEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        this.addRenderableWidget(adaptiveBtn);

        CycleButton<Boolean> particleBtn = CycleButton.onOffBuilder(config.isParticleOptimizationEnabled()).create(
            rightX, 74, buttonWidth, buttonHeight,
            Component.literal("Particle Culling"),
            (btn, val) -> {
                config.setParticleOptimizationEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        this.addRenderableWidget(particleBtn);

        CycleButton<Boolean> overlayBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            leftX, 100, buttonWidth, buttonHeight,
            Component.literal("HUD Overlay (F7)"),
            (btn, val) -> {
                config.setOverlayEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        this.addRenderableWidget(overlayBtn);

        int currentBudget = config.getParticleBudget();
        int budgetVal = PARTICLE_BUDGET_OPTIONS.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budgetVal
        ).withValues(PARTICLE_BUDGET_OPTIONS).create(
            rightX, 100, buttonWidth, buttonHeight,
            Component.literal("Particle Budget"),
            (btn, val) -> {
                config.setParticleBudget(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        this.addRenderableWidget(budgetBtn);

        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int distVal = CULL_DISTANCE_OPTIONS.contains(currentDist) ? currentDist : 32;
        CycleButton<Integer> distBtn = CycleButton.builder(
            val -> Component.literal(val + " blocks"),
            distVal
        ).withValues(CULL_DISTANCE_OPTIONS).create(
            leftX, 126, buttonWidth, buttonHeight,
            Component.literal("Cull Distance"),
            (btn, val) -> {
                config.setParticleDistanceCullSq(val * val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        this.addRenderableWidget(distBtn);

        Button resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("balanced");
            LiltClient.onConfigUpdated();
            this.rebuildWidgets();
        }).bounds(rightX, 126, buttonWidth, buttonHeight).build();
        this.addRenderableWidget(resetBtn);

        Button doneBtn = Button.builder(Component.literal("Done"), btn -> {
            LiltClient.onConfigUpdated();
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build();
        this.addRenderableWidget(doneBtn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal("Performance Workload & Telemetry Configuration"), this.width / 2, 30, 0xFFA0AAB5);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.lastScreen);
        }
    }
}
