package in.gryff.beaconwarp;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.Identifier;

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

    public NbtCompound toNbt(){
        NbtCompound newCompound = new NbtCompound();
        NbtIntArray posArray = new NbtIntArray(new int[]{pos.getX(), pos.getY(), pos.getZ()});
        newCompound.put("pos", posArray);
        newCompound.put("key", NbtString.of(key.getValue().toString()));
        return newCompound;
    }

    public static MinecraftLocation fromNbt(NbtCompound inCompound){
        NbtIntArray posArray = (NbtIntArray) inCompound.get("pos");
        NbtString keyNbt = (NbtString) inCompound.get("key");
        String keyString = keyNbt.asString();
        RegistryKey<World> worldKey = RegistryKey.of(Registry.WORLD_KEY, new Identifier(keyString));
        int[] array = posArray.getIntArray();
        BlockPos thisPos = new BlockPos(array[0], array[1], array[2]);
        return new MinecraftLocation(thisPos, worldKey);
    }
}
