package in.gryff.beaconwarp.mixin.entity;

import in.gryff.beaconwarp.BeaconWarpManager;
import in.gryff.beaconwarp.MinecraftLocation;
import in.gryff.beaconwarp.config.BeaconWarpConfig;
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

import java.util.List;
import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends LivingEntity {

    @Shadow public abstract void sendSystemMessage(Text message, UUID sender);

//    @Shadow public abstract ServerWorld getServerWorld();

    @Inject( method = "tick", at = @At("RETURN"))
    void onTick(CallbackInfo ci) {      //Run this code every tick
        if (beaconwarpCooldown != 0)
            beaconwarpCooldown--;
        if (beaconwarpCooldown == 1)
                sendSystemMessage(Text.of("Cooldown is up!"), getUuid()); //Replace this in the future with some particles?
        BlockPos posBelowPlayer = getBlockPos().down();
        if(lastPos != posBelowPlayer) {
            lastPos = posBelowPlayer;
            BlockState state = world.getBlockState(posBelowPlayer);
            Block block = state.getBlock();

            if(block instanceof BeaconBlock) {
                if (beaconwarpCooldown == 0) {
                    BeaconWarpConfig config = BeaconWarpConfig.getInstance();
                    BeaconWarpManager manager = BeaconWarpManager.get((ServerWorld) world);
                    RegistryKey<World> worldKey = world.getRegistryKey();
                    MinecraftLocation worldLocation = new MinecraftLocation(posBelowPlayer, worldKey);
                    List<Block> baseScan = manager.updateBeacon(posBelowPlayer, world);
                    MinecraftLocation teleportLocation = BeaconWarpManager.getBeaconTeleport(posBelowPlayer, world, baseScan);
                    MinecraftLocation nextTeleportLocation;
                    ServerWorld teleportWorld;

                    //We are going to update the beacon we're teleporting to. After we update it, it may have a different channel.
                    //So, we make a copy of the location, update the beacon, check if the teleport location has changed, and if it has, we repeat. We repeat until the location stays the same.
                    teleportWorld = this.getServer().getWorld(teleportLocation.getKey());
                    manager.updateBeacon(teleportLocation.getPos(),teleportWorld);
                    nextTeleportLocation = BeaconWarpManager.getBeaconTeleport(posBelowPlayer, world, baseScan);
                    while (!(nextTeleportLocation.equals(teleportLocation))){
                        teleportLocation = new MinecraftLocation(nextTeleportLocation);
                        teleportWorld = this.getServer().getWorld(teleportLocation.getKey());
                        manager.updateBeacon(teleportLocation.getPos(),teleportWorld);
                        nextTeleportLocation = BeaconWarpManager.getBeaconTeleport(posBelowPlayer, world, baseScan);
                    }
                    if (teleportLocation.equals(worldLocation)) {
                        sendSystemMessage(Text.of("Ope, teleport ain't working, sorry bud"), getUuid());
                        //This is the default cooldown
                        beaconwarpCooldown = config.Cooldown.cooldownTicks;
                    } else {
                        int cooldown;
                        //Now that we know the location we're warping to, we need to check if it's in the same world or not.
                        int beaconScore = manager.countBeaconScore(baseScan);
                        boolean scoreCanWarp = false;
                        if (nextTeleportLocation.getKeyString().equals(worldLocation.getKeyString())){
                            if (beaconScore >= config.minTeleportScore) {
                                scoreCanWarp = true;
                            } else {
                                sendSystemMessage(Text.of("Sorry, this beacon isn't powerful enough for same-dimension teleport! Its score is only " + beaconScore + "/" + config.minTeleportScore), getUuid());
                            }
                        } else if (beaconScore >= config.minInterdimensionalScore){
                            scoreCanWarp = true;
                        } else {
                            sendSystemMessage(Text.of("Sorry, this beacon isn't powerful enough for interdimensional teleport! Its score is only " + beaconScore + "/" + config.minInterdimensionalScore), getUuid());
                        }

                        if (scoreCanWarp) {
                            cooldown = manager.getCooldownTicks(beaconScore);
                            sendSystemMessage(Text.of("Teleporting player from " + worldLocation + " to " + teleportLocation), getUuid());
                            BeaconWarpManager.bwLog("Teleporting player from " + worldLocation + " to " + teleportLocation, 3);
                            double x = teleportLocation.getPos().getX() - worldLocation.getPos().getX();
                            double y = teleportLocation.getPos().getY() - worldLocation.getPos().getY();
                            double z = teleportLocation.getPos().getZ() - worldLocation.getPos().getZ();
                            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this; //this is so cursed. i hate it.
                            player.teleport(teleportWorld, getX() + x, getY() + y, getZ() + z, this.getYaw(), this.getPitch());
                            BeaconWarpManager.bwLog("Cooldown applied:" + cooldown, 3);
                            beaconwarpCooldown = cooldown;
                        }
                    }
                } else {
                    if ((beaconwarpCooldown % 20) == 0)
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