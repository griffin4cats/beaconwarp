package in.gryff.beaconwarp.mixin.entity;

import in.gryff.beaconwarp.BeaconWarpManager;
import in.gryff.beaconwarp.MinecraftLocation;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends LivingEntity {

    @Shadow public abstract void sendSystemMessage(Text message, UUID sender);

    @Shadow public abstract ServerWorld getServerWorld();

    @Inject( method = "tick", at = @At("RETURN"))
    void onTick(CallbackInfo ci) {      //Run this code every tick
        if (beaconwarpCooldown != 0)
            beaconwarpCooldown--;
        BlockPos posBelowPlayer = getBlockPos().down();
        if(lastPos != posBelowPlayer) {
            lastPos = posBelowPlayer;
            BlockState state = world.getBlockState(posBelowPlayer);
            Block block = state.getBlock();

            if(block instanceof BeaconBlock) {
                if (beaconwarpCooldown == 0) {
                    RegistryKey<World> worldKey = world.getRegistryKey();
                    MinecraftLocation worldLocation = new MinecraftLocation(posBelowPlayer, worldKey);
                    MinecraftLocation teleportLocation = BeaconWarpManager.getBeaconTeleport(posBelowPlayer, world);
                    ServerWorld teleportWorld = this.getServer().getWorld(teleportLocation.getKey());
                    //BeaconWarpManager.updateBeacon(teleportLocation.getPos(),teleportWorld);
                    if (teleportLocation.equals(worldLocation)) {
                        sendSystemMessage(Text.of("Ope, teleport ain't working, sorry bud"), getUuid());
                    } else {
                        sendSystemMessage(Text.of("Teleporting player from " + worldLocation + " to " + teleportLocation), getUuid());
                        System.out.println("Teleporting player from " + worldLocation + " to " + teleportLocation);
                        double i = teleportLocation.getPos().getX();
                        double j = teleportLocation.getPos().getY();
                        double k = teleportLocation.getPos().getZ();
                        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this; //this is so cursed. i hate it.
                        player.teleport(teleportWorld,i + .5, j + 1, k + .5, this.getYaw(), this.getPitch());
                    }
                    beaconwarpCooldown = 20;
                } else {
                    if ((beaconwarpCooldown % 5) == 0)
                        sendSystemMessage(Text.of("Sorry, you still have a cooldown for another " + beaconwarpCooldown + " ticks!"), getUuid());
                }
            }
        }
    }

    private BlockPos lastPos;
    private int beaconwarpCooldown = 0;

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }
}
