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

    private static final List<String> PROFILES = List.of("medium", "low", "high", "server", "custom");
    private static final List<Integer> SHADOW_DISTANCE_OPTIONS = List.of(24, 16, 32, -1, 0);
    private static final List<Integer> SOUND_LIMIT_OPTIONS = List.of(8, 4, 2, 0);
    private static final List<Integer> PARTICLE_BUDGET_OPTIONS = List.of(100, 300, 500, 1000, 1500, 2500, 3000);
    private static final List<Integer> CULL_DISTANCE_OPTIONS = List.of(16, 20, 24, 32, 48, 64);

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
            currentProfile = "medium";
        }

        CycleButton<String> profileBtn = CycleButton.builder(
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
            "Medium: Balanced default.\nLow: Maximum FPS for lower-end hardware.\nHigh: Visual quality for powerful PCs.\nServer: Tailored for crowded multiplayer bases.\nCustom: Manual user adjustments."
        )));
        this.addRenderableWidget(profileBtn);

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            rightX, 36, buttonWidth, buttonHeight,
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
            leftX, 58, buttonWidth, buttonHeight,
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

        CycleButton<Boolean> beaconBtn = CycleButton.onOffBuilder(config.isBeaconCulling()).create(
            rightX, 58, buttonWidth, buttonHeight,
            Component.literal("Beacon Beam Culling"),
            (btn, val) -> {
                config.setBeaconCulling(val);
                LiltClient.onConfigUpdated();
            }
        );
        beaconBtn.setTooltip(Tooltip.create(Component.literal(
            "Hides beacon and gateway beams when they are outside your view."
        )));
        this.addRenderableWidget(beaconBtn);

        int currentShadowDist = config.getShadowDistance();
        int shadowDistVal = SHADOW_DISTANCE_OPTIONS.contains(currentShadowDist) ? currentShadowDist : 24;
        CycleButton<Integer> shadowDistBtn = CycleButton.builder(
            val -> {
                if (val <= -1) return Component.literal("Default");
                if (val == 0) return Component.literal("Off");
                return Component.literal(val + " blocks");
            },
            shadowDistVal
        ).withValues(SHADOW_DISTANCE_OPTIONS).create(
            leftX, 80, buttonWidth, buttonHeight,
            Component.literal("Shadow Distance"),
            (btn, val) -> {
                config.setShadowDistance(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        shadowDistBtn.setTooltip(Tooltip.create(Component.literal(
            "Skips rendering mob shadows that are far away or in complete darkness."
        )));
        this.addRenderableWidget(shadowDistBtn);

        int currentSoundLimit = config.getSoundLimit();
        int soundLimitVal = SOUND_LIMIT_OPTIONS.contains(currentSoundLimit) ? currentSoundLimit : 8;
        CycleButton<Integer> soundLimitBtn = CycleButton.builder(
            val -> val == 0 ? Component.literal("Unlimited") : Component.literal(val + " / sec"),
            soundLimitVal
        ).withValues(SOUND_LIMIT_OPTIONS).create(
            rightX, 80, buttonWidth, buttonHeight,
            Component.literal("Sound Limiter"),
            (btn, val) -> {
                config.setSoundLimit(val);
                LiltClient.onConfigUpdated();
                this.rebuildWidgets();
            }
        );
        soundLimitBtn.setTooltip(Tooltip.create(Component.literal(
            "Prevents audio stutter by limiting repeated mob and piston sounds in crowded areas."
        )));
        this.addRenderableWidget(soundLimitBtn);

        CycleButton<Boolean> texCullBtn = CycleButton.onOffBuilder(config.isTextureCullingEnabled()).create(
            leftX, 102, buttonWidth, buttonHeight,
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

        CycleButton<Boolean> particleBtn = CycleButton.onOffBuilder(config.isParticleOptimizationEnabled()).create(
            rightX, 102, buttonWidth, buttonHeight,
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
        int budgetVal = PARTICLE_BUDGET_OPTIONS.contains(currentBudget) ? currentBudget : 1500;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budgetVal
        ).withValues(PARTICLE_BUDGET_OPTIONS).create(
            leftX, 124, buttonWidth, buttonHeight,
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
            rightX, 124, buttonWidth, buttonHeight,
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

        CycleButton<Boolean> tickGuardBtn = CycleButton.onOffBuilder(config.isTickBurstProtectionEnabled()).create(
            leftX, 146, buttonWidth, buttonHeight,
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
            rightX, 146, buttonWidth, buttonHeight,
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

        CycleButton<Boolean> overlayBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            leftX, 168, buttonWidth, buttonHeight,
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
            config.setActiveProfile("medium");
            LiltClient.onConfigUpdated();
            this.rebuildWidgets();
        }).bounds(rightX, 168, buttonWidth, buttonHeight).build();
        resetBtn.setTooltip(Tooltip.create(Component.literal("Restores all settings back to the default Medium profile.")));
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
        graphics.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.lastScreen);
        }
    }
}
