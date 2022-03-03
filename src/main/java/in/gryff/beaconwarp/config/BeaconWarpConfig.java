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

        @ConfigEntry.Gui.Tooltip()
        public int scoreIron = 2;

        @ConfigEntry.Gui.Tooltip()
        public int scoreGold = 3;

        @ConfigEntry.Gui.Tooltip()
        public int scoreEmerald = 5;

        @ConfigEntry.Gui.Tooltip()
        public int scoreDiamond = 20;

        @ConfigEntry.Gui.Tooltip()
        public int scoreNetherite = 80;

        @ConfigEntry.Gui.Tooltip()
        public int cooldownMinTicks = 100;

        @ConfigEntry.Gui.Tooltip()
        public int cooldownMaxTicks = 900;

        @ConfigEntry.Gui.Tooltip()
        public int cooldownMinScore = 329;

        @ConfigEntry.Gui.Tooltip()
        public int cooldownMaxScore = 13120;

    }

    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.Tooltip(count = 4)
    public int minTeleportScore = 329;

    @ConfigEntry.Category(value = "mechanics")
    @ConfigEntry.Gui.Tooltip(count = 4)
    public int minInterdimensionalScore = 493;

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

    //@ConfigEntry.Gui.Excluded //Unimplemented for now
    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip()
    public int loggingType = 1;

    @ConfigEntry.Category(value = "misc")
    @ConfigEntry.Gui.Tooltip()
    public boolean RESET_ALL_INFO = false;

    public static void init() {
        AutoConfig.register(BeaconWarpConfig.class, GsonConfigSerializer::new);
    }

    public static BeaconWarpConfig getInstance() {
        return AutoConfig.getConfigHolder(BeaconWarpConfig.class).getConfig();
    }
}