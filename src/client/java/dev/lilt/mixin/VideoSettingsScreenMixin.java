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

        List<String> profiles = List.of("medium", "low", "high", "server", "custom");
        String currentProfile = config.getActiveProfile().toLowerCase();
        if (!profiles.contains(currentProfile)) {
            currentProfile = "medium";
        }

        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] adaptiveHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] animLodHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Boolean>[] beaconHolder = (CycleButton<Boolean>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] shadowDistHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];
        @SuppressWarnings("unchecked")
        final CycleButton<Integer>[] soundLimitHolder = (CycleButton<Integer>[]) new CycleButton<?>[1];
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
                if (beaconHolder[0] != null) beaconHolder[0].setValue(config.isBeaconCulling());
                if (shadowDistHolder[0] != null) shadowDistHolder[0].setValue(config.getShadowDistance());
                if (soundLimitHolder[0] != null) soundLimitHolder[0].setValue(config.getSoundLimit());
                if (texCullHolder[0] != null) texCullHolder[0].setValue(config.isTextureCullingEnabled());
                if (tickGuardHolder[0] != null) tickGuardHolder[0].setValue(config.isTickBurstProtectionEnabled());
                if (gcGuardHolder[0] != null) gcGuardHolder[0].setValue(config.isGcGuardEnabled());
                if (particleHolder[0] != null) particleHolder[0].setValue(config.isParticleOptimizationEnabled());
                if (budgetHolder[0] != null) budgetHolder[0].setValue(config.getParticleBudget());
                if (distHolder[0] != null) distHolder[0].setValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())));
            }
        );
        profileBtn.setTooltip(Tooltip.create(Component.literal(
            "Medium: Balanced default.\nLow: Maximum FPS for lower-end hardware.\nHigh: Visual quality for powerful PCs.\nServer: Tailored for crowded multiplayer bases.\nCustom: Manual user adjustments."
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
            "Adjusts animation update rates for distant and off-screen mobs and players based on your camera view."
        )));
        animLodHolder[0] = animLodBtn;

        CycleButton<Boolean> beaconBtn = CycleButton.onOffBuilder(config.isBeaconCulling()).create(
            Component.literal("Beacon Beam Culling"),
            (btn, val) -> {
                config.setBeaconCulling(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        beaconBtn.setTooltip(Tooltip.create(Component.literal(
            "Hides beacon and gateway beams when they are outside your view."
        )));
        beaconHolder[0] = beaconBtn;

        this.list.addSmall(animLodBtn, beaconBtn);

        List<Integer> shadowDistOptions = List.of(24, 16, 32, -1, 0);
        int currentShadow = config.getShadowDistance();
        int shadowVal = shadowDistOptions.contains(currentShadow) ? currentShadow : 24;
        CycleButton<Integer> shadowDistBtn = CycleButton.builder(
            val -> {
                if (val <= -1) return Component.literal("Default");
                if (val == 0) return Component.literal("Off");
                return Component.literal(val + " blocks");
            },
            shadowVal
        ).withValues(shadowDistOptions).create(
            Component.literal("Shadow Distance"),
            (btn, val) -> {
                config.setShadowDistance(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        shadowDistBtn.setTooltip(Tooltip.create(Component.literal(
            "Skips rendering mob shadows that are far away or in complete darkness."
        )));
        shadowDistHolder[0] = shadowDistBtn;

        List<Integer> soundLimitOptions = List.of(8, 4, 2, 0);
        int currentLimit = config.getSoundLimit();
        int limitVal = soundLimitOptions.contains(currentLimit) ? currentLimit : 8;
        CycleButton<Integer> soundLimitBtn = CycleButton.builder(
            val -> val == 0 ? Component.literal("Unlimited") : Component.literal(val + " / sec"),
            limitVal
        ).withValues(soundLimitOptions).create(
            Component.literal("Sound Limiter"),
            (btn, val) -> {
                config.setSoundLimit(val);
                config.setActiveProfile("custom");
                profileBtn.setValue("custom");
                LiltClient.onConfigUpdated();
            }
        );
        soundLimitBtn.setTooltip(Tooltip.create(Component.literal(
            "Prevents audio stutter by limiting repeated mob and piston sounds in crowded areas."
        )));
        soundLimitHolder[0] = soundLimitBtn;

        this.list.addSmall(shadowDistBtn, soundLimitBtn);

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

        this.list.addSmall(texCullBtn, particleBtn);

        List<Integer> budgetOptions = List.of(100, 300, 500, 1000, 1500, 2500, 3000);
        int currentBudget = config.getParticleBudget();
        int budget = budgetOptions.contains(currentBudget) ? currentBudget : 1500;
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

        List<Integer> distOptions = List.of(16, 20, 24, 32, 48, 64);
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

        this.list.addSmall(budgetBtn, distBtn);

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

        Button resetBtn = Button.builder(Component.literal("Reset Defaults"), btn -> {
            config.setActiveProfile("medium");
            LiltClient.onConfigUpdated();
            profileBtn.setValue("medium");
            if (adaptiveHolder[0] != null) adaptiveHolder[0].setValue(config.isAdaptiveEnabled());
            if (animLodHolder[0] != null) animLodHolder[0].setValue(config.isAnimationLodEnabled());
            if (beaconHolder[0] != null) beaconHolder[0].setValue(config.isBeaconCulling());
            if (shadowDistHolder[0] != null) shadowDistHolder[0].setValue(config.getShadowDistance());
            if (soundLimitHolder[0] != null) soundLimitHolder[0].setValue(config.getSoundLimit());
            if (texCullHolder[0] != null) texCullHolder[0].setValue(config.isTextureCullingEnabled());
            if (tickGuardHolder[0] != null) tickGuardHolder[0].setValue(config.isTickBurstProtectionEnabled());
            if (gcGuardHolder[0] != null) gcGuardHolder[0].setValue(config.isGcGuardEnabled());
            if (particleHolder[0] != null) particleHolder[0].setValue(config.isParticleOptimizationEnabled());
            if (budgetHolder[0] != null) budgetHolder[0].setValue(config.getParticleBudget());
            if (distHolder[0] != null) distHolder[0].setValue((int) Math.round(Math.sqrt(config.getParticleDistanceCullSq())));
        }).build();
        resetBtn.setTooltip(Tooltip.create(Component.literal(
            "Restores all Lilt settings back to the default Medium profile."
        )));

        this.list.addSmall(hudBtn, resetBtn);
    }
}
