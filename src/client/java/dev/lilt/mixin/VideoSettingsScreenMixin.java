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
@Mixin(value = VideoSettingsScreen.class, remap = false)
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

        CycleButton<String> profileBtn = CycleButton.builder(
            val -> Component.literal(val.substring(0, 1).toUpperCase() + val.substring(1)),
            currentProfile
        ).withValues(profiles).create(
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
            "Balanced: Recommended baseline.\nLow: Maximum FPS for lower-end PCs.\nPerformance: High quality settings.\nCustom: Manual user adjustments."
        )));

        CycleButton<Boolean> adaptiveBtn = CycleButton.onOffBuilder(config.isAdaptiveEnabled()).create(
            Component.literal("Dynamic Particle Scaling"),
            (btn, val) -> {
                config.setAdaptiveEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        adaptiveBtn.setTooltip(Tooltip.create(Component.literal(
            "Temporarily reduces particle counts during lag spikes to help keep framerates steady."
        )));
        adaptiveHolder[0] = adaptiveBtn;

        this.list.addSmall(profileBtn, adaptiveBtn);

        CycleButton<Boolean> animLodBtn = CycleButton.onOffBuilder(config.isAnimationLodEnabled()).create(
            Component.literal("Smart Entity Animations"),
            (btn, val) -> {
                config.setAnimationLodEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        animLodBtn.setTooltip(Tooltip.create(Component.literal(
            "Adjusts animation update rates for distant and off-screen mobs and players based on your camera view. Saves CPU in crowded areas."
        )));
        animLodHolder[0] = animLodBtn;

        CycleButton<Boolean> texCullBtn = CycleButton.onOffBuilder(config.isTextureCullingEnabled()).create(
            Component.literal("Optimized Texture Updates"),
            (btn, val) -> {
                config.setTextureCullingEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        texCullBtn.setTooltip(Tooltip.create(Component.literal(
            "Pauses animated textures like water, lava, and fire while paused or during severe lag."
        )));
        texCullHolder[0] = texCullBtn;

        this.list.addSmall(animLodBtn, texCullBtn);

        CycleButton<Boolean> tickGuardBtn = CycleButton.onOffBuilder(config.isTickBurstProtectionEnabled()).create(
            Component.literal("Server Catch-Up Limiter"),
            (btn, val) -> {
                config.setTickBurstProtectionEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        tickGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Prevents singleplayer server tick bursts from freezing the client render thread."
        )));
        tickGuardHolder[0] = tickGuardBtn;

        CycleButton<Boolean> gcGuardBtn = CycleButton.onOffBuilder(config.isGcGuardEnabled()).create(
            Component.literal("Memory Cleanup on Pause"),
            (btn, val) -> {
                config.setGcGuardEnabled(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        gcGuardBtn.setTooltip(Tooltip.create(Component.literal(
            "Reclaims unused Java memory when paused if memory usage is high."
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
            "Hides particles that are outside your render distance."
        )));
        particleHolder[0] = particleBtn;

        List<Integer> budgetOptions = List.of(100, 300, 500, 1000, 1500, 2500);
        int currentBudget = config.getParticleBudget();
        int budget = budgetOptions.contains(currentBudget) ? currentBudget : 1000;
        CycleButton<Integer> budgetBtn = CycleButton.builder(
            val -> Component.literal(String.valueOf(val)),
            budget
        ).withValues(budgetOptions).create(
            Component.literal("Particle Limit"),
            (btn, val) -> {
                config.setParticleBudget(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        budgetBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum number of active particles allowed at one time."
        )));
        budgetHolder[0] = budgetBtn;

        this.list.addSmall(particleBtn, budgetBtn);

        List<Integer> distOptions = List.of(16, 20, 32, 48, 64);
        int currentDist = (int) Math.round(Math.sqrt(config.getParticleDistanceCullSq()));
        int dist = distOptions.contains(currentDist) ? currentDist : 32;
        CycleButton<Integer> distBtn = CycleButton.builder(
            val -> Component.literal(val + " blocks"),
            dist
        ).withValues(distOptions).create(
            Component.literal("Particle Distance"),
            (btn, val) -> {
                config.setParticleDistanceCullSq(val * val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        distBtn.setTooltip(Tooltip.create(Component.literal(
            "Maximum distance from the camera to render particles."
        )));
        distHolder[0] = distBtn;

        CycleButton<Boolean> hudBtn = CycleButton.onOffBuilder(config.isOverlayEnabled()).create(
            Component.literal("Performance Overlay (F7)"),
            (btn, val) -> {
                config.setOverlayEnabled(val);
                LiltClient.onConfigUpdated();
            }
        );
        hudBtn.setTooltip(Tooltip.create(Component.literal(
            "Shows an on-screen overlay with FPS, frametimes, and memory status."
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
