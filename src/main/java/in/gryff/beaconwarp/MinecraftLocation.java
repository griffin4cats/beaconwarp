package in.gryff.beaconwarp;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class MinecraftLocation {
    BlockPos pos;
    RegistryKey<World> key;

    public MinecraftLocation(BlockPos posIn, RegistryKey keyIn){
        pos = posIn;
        key = keyIn;
    }

    public RegistryKey<World> getKey(){
        return key;
    }

    public BlockPos getPos(){
        return pos;
    }

    public String toString(){
        return key.toString() + pos.toString();
    }
}
