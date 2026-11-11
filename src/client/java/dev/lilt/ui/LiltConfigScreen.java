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

    private CycleButton<String> profileBtn;
    private CycleButton<Integer> fpsBtn;
    private CycleButton<Boolean> adaptiveBtn;
    private CycleButton<Boolean> particleBtn;
    private CycleButton<Boolean> overlayBtn;
    private CycleButton<Integer> budgetBtn;
    private CycleButton<Integer> distBtn;
    private Button resetBtn;
    private Button doneBtn;

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

        profileBtn = CycleButton.builder(
            val -> Component.literal(val.substring(0, 1).toUpperCase() + val.substring(1)),
            currentProfile
        ).withValues(PROFILES).create(
            leftX, 36, buttonWidth, buttonHeight,
            Component.literal("Profile"),
            (btn, val) -> {
                config.setActiveProfile(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        profileBtn.setTooltip(Tooltip.create(Component.literal(
            "Balanced: Recommended baseline (60 FPS, 1000 particles).\n" +
            "Low: Maximum performance savings for low-spec PCs.\n" +
            "Performance: Extended fidelity for high-end GPUs.\n" +
            "Custom: Preserves your manual adjustments."
        )));
        this.addRenderableWidget(profileBtn);

        int currentFps = config.getTargetFps();
        int targetIndex = TARGET_FPS_OPTIONS.contains(currentFps) ? currentFps : 60;
        fpsBtn = CycleButton.builder(
            val -> Component.literal(val >= 260 ? "Unlimited" : val + " FPS"),
            targetIndex
        ).withValues(TARGET_FPS_OPTIONS).create(
            rightX, 36, buttonWidth, buttonHeight,
            Component.literal("Target FPS"),
            (btn, val) -> {
                config.setTargetFps(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        fpsBtn.setTooltip(Tooltip.create(Component.literal(
            "The framerate threshold monitored by Adaptive Tuning.\n" +
            "When FPS falls below this target, particle load is dynamically trimmed."
        )));
        this.addRenderableWidget(fpsBtn);

        adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            leftX, 60, buttonWidth, buttonHeight,
            Component.literal("Adaptive Tuning"),
            (btn, val) -> {
                config.setAdaptiveEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        adaptiveBtn.setTooltip(Tooltip.create(Component.literal(
            "ON: Dynamically trims particle budgets during severe frame drops.\n" +
            "OFF: Particle limits remain strictly static at their maximum."
        )));
        this.addRenderableWidget(adaptiveBtn);

        particleBtn = CycleButton.onOffBuilder(config.isParticleOptimizationEnabled()).create(
            rightX, 60, buttonWidth, buttonHeight,
            Component.literal("Particle Culling"),
            (btn, val) -> {
                config.setParticleOptimizationEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        particleBtn.setTooltip(Tooltip.create(Component.literal(
            "ON: Skips rendering of distant particles outside culling radius.\n" +
            "Particles within 6 blocks of your camera are always protected."
        )));
        this.addRenderableWidget(particleBtn);

        overlayBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            leftX, 84, buttonWidth, buttonHeight,
            Component.literal("HUD Overlay (F7)"),
            (btn, val) -> {
                config.setOverlayEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        overlayBtn.setTooltip(Tooltip.create(Component.literal(
            "Displays live telemetry HUD: FPS, P50/P99 frame times, tick durations, GC pauses, and bottleneck diagnosis."
        )));
        this.addRenderableWidget(overlayBtn);

        int currentBudget = config.getParticleBudget();
        int budgetVal = PARTICLE_BUDGET_OPTIONS.contains(currentBudget) ? currentBudget : 1000;
        budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budgetVal
        ).withValues(PARTICLE_BUDGET_OPTIONS).create(
            rightX, 84, buttonWidth, buttonHeight,
            Component.literal("Particle Budget"),
            (btn, val) -> {
                config.setParticleBudget(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        budgetBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum simultaneous active particles permitted.\n" +
            "100 - 500: Prevents lag in explosive fights and mob spawners.\n" +
            "1000: Balanced default.\n" +
            "2500+: Cinematic density for strong GPUs."
        )));
        this.addRenderableWidget(budgetBtn);

        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int distVal = CULL_DISTANCE_OPTIONS.contains(currentDist) ? currentDist : 32;
        distBtn = CycleButton.builder(
            val -> Component.literal(val + " blocks"),
            distVal
        ).withValues(CULL_DISTANCE_OPTIONS).create(
            leftX, 108, buttonWidth, buttonHeight,
            Component.literal("Cull Distance"),
            (btn, val) -> {
                config.setParticleDistanceCullSq(val * val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        distBtn.setTooltip(Tooltip.create(Component.literal(
            "Distance in blocks beyond which particles are not created.\n" +
            "16 - 20m: High performance savings on low-end hardware.\n" +
            "32m: Balanced view distance.\n" +
            "48 - 64m: Extended visibility."
        )));
        this.addRenderableWidget(distBtn);

        resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("balanced");
            LiltClient.onConfigUpdated();
            this.rebuildWidgets();
        }).bounds(rightX, 108, buttonWidth, buttonHeight).build();
        resetBtn.setTooltip(Tooltip.create(Component.literal("Restores all settings back to the default Balanced profile.")));
        this.addRenderableWidget(resetBtn);

        doneBtn = Button.builder(Component.literal("Done"), btn -> {
            LiltClient.onConfigUpdated();
            this.onClose();
        }).bounds(this.width / 2 - 100, this.height - 26, 200, 20).build();
        doneBtn.setTooltip(Tooltip.create(Component.literal("Saves changes immediately to disk and applies them to the game.")));
        this.addRenderableWidget(doneBtn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, this.width / 2, 12, 0xFF64B5F6);
        graphics.centeredText(this.font, Component.literal("Client Workload & Frame Pacing Configuration"), this.width / 2, 23, 0xFF8A94A0);

        int cardX = this.width / 2 - 155;
        int cardY = 134;
        int cardW = 310;
        int cardH = Math.max(54, this.height - cardY - 32);

        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, 0xD012151B);
        graphics.outline(cardX, cardY, cardW, cardH, 0x503C4452);
        graphics.fill(cardX, cardY, cardX + cardW, cardY + 2, 0xFF64B5F6);

        String hoverTitle;
        String hoverDesc;
        String hoverImpact;

        if (profileBtn != null && profileBtn.isHoveredOrFocused()) {
            hoverTitle = "Profile Preset";
            hoverDesc = "Pre-configured tuning presets for diverse hardware levels.";
            hoverImpact = "Balanced (60 FPS / 1000 max), Low (30 FPS / 300 max), Performance (120 FPS / 2500 max).";
        } else if (fpsBtn != null && fpsBtn.isHoveredOrFocused()) {
            hoverTitle = "Target Framerate";
            hoverDesc = "The performance target used by Adaptive Tuning to detect frame drops.";
            hoverImpact = "Higher targets react faster to slowdowns; lower targets tolerate frame drops before trimming.";
        } else if (adaptiveBtn != null && adaptiveBtn.isHoveredOrFocused()) {
            hoverTitle = "Adaptive Tuning";
            hoverDesc = "Dynamically adjusts particle workloads during sudden frametime spikes.";
            hoverImpact = "Suppresses particle bursts during heavy explosions and wither fights without changing graphic options.";
        } else if (particleBtn != null && particleBtn.isHoveredOrFocused()) {
            hoverTitle = "Particle Distance Culling";
            hoverDesc = "Cancels rendering of distant particles outside your camera view distance.";
            hoverImpact = "Significantly reduces draw calls. Particles within 6 blocks of your player are always protected.";
        } else if (overlayBtn != null && overlayBtn.isHoveredOrFocused()) {
            hoverTitle = "Performance HUD Overlay (F7)";
            hoverDesc = "In-game real-time diagnostics HUD showing live metrics and bottleneck analysis.";
            hoverImpact = "Cached every 250ms to ensure zero allocation overhead during rendering.";
        } else if (budgetBtn != null && budgetBtn.isHoveredOrFocused()) {
            hoverTitle = "Particle Budget";
            hoverDesc = "The global limit of simultaneous active particles allowed across the world.";
            hoverImpact = "Lower values eliminate framerate dips in mob farms and fire scenes. High values maximize visual density.";
        } else if (distBtn != null && distBtn.isHoveredOrFocused()) {
            hoverTitle = "Particle Culling Distance";
            hoverDesc = "Radius in blocks beyond which particle creation is cancelled.";
            hoverImpact = "16m - 20m for low-spec PCs; 32m is balanced; 64m for open landscapes.";
        } else if (resetBtn != null && resetBtn.isHoveredOrFocused()) {
            hoverTitle = "Reset Defaults";
            hoverDesc = "Resets all settings to the factory Balanced configuration.";
            hoverImpact = "Safe recovery option if custom values degrade gameplay.";
        } else if (doneBtn != null && doneBtn.isHoveredOrFocused()) {
            hoverTitle = "Save & Apply";
            hoverDesc = "Saves your configuration to .minecraft/config/lilt.json and applies changes immediately.";
            hoverImpact = "No Minecraft restart is required.";
        } else {
            hoverTitle = "Lilt Optimization Suite";
            hoverDesc = "Hover over any setting above to inspect its technical details and performance impact.";
            hoverImpact = "Fully compatible with Sodium, Lithium, Iris, and FerriteCore.";
        }

        graphics.text(this.font, hoverTitle, cardX + 8, cardY + 7, 0xFF64B5F6);
        graphics.textWithWordWrap(this.font, Component.literal(hoverDesc), cardX + 8, cardY + 20, cardW - 16, 0xFFE0E6ED);
        if (cardH >= 64) {
            graphics.textWithWordWrap(this.font, Component.literal(hoverImpact), cardX + 8, cardY + 36, cardW - 16, 0xFF81C784);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.lastScreen);
        }
    }
}
