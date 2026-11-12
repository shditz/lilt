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

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            rightX, 40, buttonWidth, buttonHeight,
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

        CycleButton<Boolean> animLodBtn = CycleButton.onOffBuilder(config.isAnimationLodEnabled()).create(
            leftX, 64, buttonWidth, buttonHeight,
            Component.literal("Animation LOD"),
            (btn, val) -> {
                config.setAnimationLodEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        animLodBtn.setTooltip(Tooltip.create(Component.literal(
            "Throttles distant entity model animations based on distance. Greatly improves FPS in villages and farms."
        )));
        this.addRenderableWidget(animLodBtn);

        CycleButton<Boolean> texCullBtn = CycleButton.onOffBuilder(config.isTextureCullingEnabled()).create(
            rightX, 64, buttonWidth, buttonHeight,
            Component.literal("Texture Optimization"),
            (btn, val) -> {
                config.setTextureCullingEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        texCullBtn.setTooltip(Tooltip.create(Component.literal(
            "Throttles animated texture buffer uploads across PCIe during frametime spikes and while paused."
        )));
        this.addRenderableWidget(texCullBtn);

        CycleButton<Boolean> tickGuardBtn = CycleButton.onOffBuilder(config.isTickBurstProtectionEnabled()).create(
            leftX, 88, buttonWidth, buttonHeight,
            Component.literal("Tick Burst Limiter"),
            (btn, val) -> {
                config.setTickBurstProtectionEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        tickGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Prevents singleplayer internal server catch-up tick bursts from locking the CPU and freezing rendering."
        )));
        this.addRenderableWidget(tickGuardBtn);

        CycleButton<Boolean> gcGuardBtn = CycleButton.onOffBuilder(config.isGcGuardEnabled()).create(
            rightX, 88, buttonWidth, buttonHeight,
            Component.literal("Memory Guard"),
            (btn, val) -> {
                config.setGcGuardEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        gcGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Proactively triggers garbage collection while paused if memory pressure is high to prevent in-game freezes."
        )));
        this.addRenderableWidget(gcGuardBtn);

        CycleButton<Boolean> particleBtn = CycleButton.onOffBuilder(config.isParticleOptimizationEnabled()).create(
            leftX, 112, buttonWidth, buttonHeight,
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

        int currentBudget = config.getParticleBudget();
        int budgetVal = PARTICLE_BUDGET_OPTIONS.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budgetVal
        ).withValues(PARTICLE_BUDGET_OPTIONS).create(
            rightX, 112, buttonWidth, buttonHeight,
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
            leftX, 136, buttonWidth, buttonHeight,
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

        CycleButton<Boolean> overlayBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            rightX, 136, buttonWidth, buttonHeight,
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

        Button resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("balanced");
            LiltClient.onConfigUpdated();
            this.rebuildWidgets();
        }).bounds(leftX, 160, buttonWidth, buttonHeight).build();
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