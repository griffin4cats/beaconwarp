package in.gryff.beaconwarp;

import net.fabricmc.api.ModInitializer;
import in.gryff.beaconwarp.config.BeaconWarpConfig;

public class BeaconWarp implements ModInitializer {
    public static final String MODID = "beaconwarp";
    public static final String MOD_NAME = "Beacon Warp";

    @Override
    public void onInitialize() {
        System.out.println("BeaconWarp onInitialize()!");
        BeaconWarpConfig.init();
    }
}
