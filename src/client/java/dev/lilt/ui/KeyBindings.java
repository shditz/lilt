package dev.lilt.ui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.lilt.config.LiltConfig;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

public class KeyBindings {

    private static KeyMapping toggleOverlay;

    public static void register() {
        toggleOverlay = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.lilt.toggle_overlay",
            InputConstants.KEY_F7,
            KeyMapping.Category.MISC
        ));
    }

    public static void handleInput(LiltConfig config, OverlayRenderer overlay) {
        if (toggleOverlay == null) return;
        while (toggleOverlay.consumeClick()) {
            overlay.toggle();
            config.setOverlayEnabled(overlay.isVisible());
        }
    }
}
