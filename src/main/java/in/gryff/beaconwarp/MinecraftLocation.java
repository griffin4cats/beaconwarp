package in.gryff.beaconwarp;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class MinecraftLocation {
    final BlockPos pos;
    final RegistryKey<World> key;

    public MinecraftLocation(BlockPos posIn, RegistryKey<World> keyIn){
        pos = new BlockPos (posIn);
        key = keyIn;
    }

    public MinecraftLocation(MinecraftLocation location){
        this(location.getPos(), location.getKey());
    }

    public RegistryKey<World> getKey(){
        return key;
    }

    public BlockPos getPos(){
        return pos;
    }

    public String toString(){
        return pos.toString() + " in " + key.getValue().toString();
    }

    public boolean equals(MinecraftLocation otherLocation){
        return (otherLocation.toString().equals(this.toString()));
    }
}
