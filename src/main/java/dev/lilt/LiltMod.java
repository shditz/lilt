package dev.lilt;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiltMod implements ModInitializer {

    public static final String MOD_ID = "lilt";
    public static final String MOD_NAME = "Lilt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("Lilt initialized");
    }
}
