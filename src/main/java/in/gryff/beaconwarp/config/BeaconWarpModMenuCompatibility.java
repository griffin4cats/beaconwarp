package in.gryff.beaconwarp.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BeaconWarpModMenuCompatibility implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        System.out.println("Well at least the modmenu compat thing is getting run?");
        return parent -> AutoConfig.getConfigScreen(BeaconWarpConfig.class, parent).get();
    }
}