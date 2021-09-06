package in.gryff.beaconwarp;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment( EnvType.SERVER )
public class BeaconWarp implements DedicatedServerModInitializer {

    public static final String MODID = "beaconwarp";

    @Override
    public void onInitializeServer( )
    {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
    }

    public static ModConfig getConfig() {
        if (config == null) {
            config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        }
        return config;
    }

    private static ModConfig config;

    @Config(name = MODID)
    public static class ModConfig implements ConfigData {
        @Comment("Toggles the entire mod on or off")
        boolean enabled = true;
    }
}
