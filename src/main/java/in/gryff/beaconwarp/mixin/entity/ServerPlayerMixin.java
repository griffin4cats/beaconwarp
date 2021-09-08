package in.gryff.beaconwarp.mixin.entity;

import in.gryff.beaconwarp.BeaconWarpManager;
import net.minecraft.block.BeaconBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
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
                    Pair<BlockPos, RegistryKey<World>> worldPosPair = new Pair<>(posBelowPlayer, worldKey);
                    Pair<BlockPos, RegistryKey<World>> teleportPosPair = BeaconWarpManager.getBeaconTeleport(posBelowPlayer, world);
                    if (teleportPosPair.equals(worldPosPair))
                        sendSystemMessage(Text.of("Ope, teleport ain't workin, sorry bud"), getUuid());
                    else {
                        double i = teleportPosPair.getLeft().getX();
                        double j = teleportPosPair.getLeft().getY();
                        double k = teleportPosPair.getLeft().getZ();
                        ServerWorld teleportWorld = this.getServer().getWorld(teleportPosPair.getRight());
                        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
                        player.teleport(teleportWorld,i + .5, j + 1, k + .5, this.getYaw(), this.getPitch());
                        beaconwarpCooldown = 10;
                    }
                } else {
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
