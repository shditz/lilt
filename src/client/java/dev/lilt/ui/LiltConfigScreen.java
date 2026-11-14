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
            "Balanced: Recommended baseline.\nLow: Maximum FPS for lower-end PCs.\nPerformance: High quality settings.\nCustom: Manual user adjustments."
        )));
        this.addRenderableWidget(profileBtn);

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            rightX, 40, buttonWidth, buttonHeight,
            Component.literal("Dynamic Particle Scaling"),
            (btn, val) -> {
                config.setAdaptiveEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        adaptiveBtn.setTooltip(Tooltip.create(Component.literal(
            "Temporarily reduces particle counts during lag spikes to help keep framerates steady."
        )));
        this.addRenderableWidget(adaptiveBtn);

        CycleButton<Boolean> animLodBtn = CycleButton.onOffBuilder(config.isAnimationLodEnabled()).create(
            leftX, 64, buttonWidth, buttonHeight,
            Component.literal("Smart Entity Animations"),
            (btn, val) -> {
                config.setAnimationLodEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        animLodBtn.setTooltip(Tooltip.create(Component.literal(
            "Adjusts animation update rates for distant and off-screen mobs and players based on your camera view."
        )));
        this.addRenderableWidget(animLodBtn);

        CycleButton<Boolean> texCullBtn = CycleButton.onOffBuilder(config.isTextureCullingEnabled()).create(
            rightX, 64, buttonWidth, buttonHeight,
            Component.literal("Optimized Texture Updates"),
            (btn, val) -> {
                config.setTextureCullingEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        texCullBtn.setTooltip(Tooltip.create(Component.literal(
            "Pauses animated textures like water, lava, and fire while paused or during severe lag."
        )));
        this.addRenderableWidget(texCullBtn);

        CycleButton<Boolean> tickGuardBtn = CycleButton.onOffBuilder(config.isTickBurstProtectionEnabled()).create(
            leftX, 88, buttonWidth, buttonHeight,
            Component.literal("Server Catch-Up Limiter"),
            (btn, val) -> {
                config.setTickBurstProtectionEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        tickGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Prevents singleplayer server tick bursts from freezing the client render thread."
        )));
        this.addRenderableWidget(tickGuardBtn);

        CycleButton<Boolean> gcGuardBtn = CycleButton.onOffBuilder(config.isGcGuardEnabled()).create(
            rightX, 88, buttonWidth, buttonHeight,
            Component.literal("Memory Cleanup on Pause"),
            (btn, val) -> {
                config.setGcGuardEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        gcGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Reclaims unused Java memory when paused if memory usage is high."
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
            "Hides particles that are outside your render distance."
        )));
        this.addRenderableWidget(particleBtn);

        int currentBudget = config.getParticleBudget();
        int budgetVal = PARTICLE_BUDGET_OPTIONS.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budgetVal
        ).withValues(PARTICLE_BUDGET_OPTIONS).create(
            rightX, 112, buttonWidth, buttonHeight,
            Component.literal("Particle Limit"),
            (btn, val) -> {
                config.setParticleBudget(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        budgetBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum number of active particles allowed at one time."
        )));
        this.addRenderableWidget(budgetBtn);

        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int distVal = CULL_DISTANCE_OPTIONS.contains(currentDist) ? currentDist : 32;
        CycleButton<Integer> distBtn = CycleButton.builder(
            val -> Component.literal(val + " blocks"),
            distVal
        ).withValues(CULL_DISTANCE_OPTIONS).create(
            leftX, 136, buttonWidth, buttonHeight,
            Component.literal("Particle Distance"),
            (btn, val) -> {
                config.setParticleDistanceCullSq(val * val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        distBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum distance from the camera to render particles."
        )));
        this.addRenderableWidget(distBtn);

        CycleButton<Boolean> overlayBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            rightX, 136, buttonWidth, buttonHeight,
            Component.literal("Performance Overlay (F7)"),
            (btn, val) -> {
                config.setOverlayEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        overlayBtn.setTooltip(Tooltip.create(Component.literal(
            "Shows an on-screen overlay with FPS, frametimes, and memory status."
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
