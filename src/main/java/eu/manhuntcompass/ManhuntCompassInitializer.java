package eu.manhuntcompass;

import net.fabricmc.api.ModInitializer;

public class ManhuntCompassInitializer implements ModInitializer {

    @Override
    public void onInitialize() {
        ManhuntCompass.INSTANCE.onInitialize();
    }
}
