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

    public MinecraftLocation(MinecraftLocation location){
        pos = location.getPos();
        key = location.getKey();
    }

    public RegistryKey<World> getKey(){
        return key;
    }

    public BlockPos getPos(){
        return pos;
    }

    public String toString(){
        return pos.toString().substring(8) + " in " + key.getValue().toString();
    }

    public boolean equals(MinecraftLocation otherLocation){
        return (otherLocation.toString().equals(this.toString()));
    }
}
