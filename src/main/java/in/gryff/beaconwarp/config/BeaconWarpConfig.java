package in.gryff.beaconwarp.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.sound.SoundEvents;


@Config(name = "beaconwarp")
@Config.Gui.Background(value = "minecraft:textures/block/beacon.png")
public class BeaconWarpConfig implements ConfigData {
    //Variables to (possibly) add:
    //Activation item
    //Valid blocks
    //Particle effects
    //Fleshed out logging config

    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.CollapsibleObject
    public Cooldown Cooldown = new Cooldown();

    public static class Cooldown{
        @ConfigEntry.Gui.Tooltip()
        public int cooldownTicks = 10;

        @ConfigEntry.Gui.Excluded //Unimplemented for now
        @ConfigEntry.Gui.Tooltip()
        public boolean cooldownSameWarpEnabled = false;

        @ConfigEntry.Gui.Excluded //Unimplemented for now
        @ConfigEntry.Gui.Tooltip()
        public int cooldownSameWarpTicks = 10;
    }

    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.Tooltip(count = 4)
    public int maxBeaconLevel = 4;

    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.Tooltip()
    public boolean allowRotate = true;

    @ConfigEntry.Gui.Excluded //Unimplemented for now
    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.Tooltip()
    public boolean allowReflect = false;    

    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.Tooltip()
    public String soundActivate = SoundEvents.BLOCK_BEACON_POWER_SELECT.getId().toString();

    @ConfigEntry.Gui.Excluded //Unimplemented for now
    @ConfigEntry.Category(value = "logging")
    @ConfigEntry.Gui.Tooltip()
    public boolean doAllLogging = true;

    public static void init() {
        AutoConfig.register(BeaconWarpConfig.class, GsonConfigSerializer::new);
    }

    public static BeaconWarpConfig getInstance() {
        return AutoConfig.getConfigHolder(BeaconWarpConfig.class).getConfig();
    }
}