package in.gryff.beaconwarp.mixin.block;

import in.gryff.beaconwarp.BeaconWarpManager;
import net.minecraft.block.Block;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;


import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(at=@At("HEAD"), method="onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V")
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player, CallbackInfo ci) {
        if (!world.isClient && state.getBlock().getTranslationKey().equals("block.minecraft.beacon")){
            BeaconWarpManager manager = BeaconWarpManager.get((ServerWorld) world);
            if (manager.isWarpBeacon(pos, world))
                manager.removeBeaconWithLocation(pos, world);
        }
    }
}