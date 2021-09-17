package in.gryff.beaconwarp.mixin.block;

import in.gryff.beaconwarp.BeaconWarpManager;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BeaconBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(BeaconBlock.class)
public class BeaconBlockMixin {

    @Inject(at=@At("HEAD"), method="onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;",
            cancellable=true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> ci) {
        if (!world.isClient) {
            player.sendSystemMessage(Text.of("Right clicked a beacon"),player.getUuid());
            ItemStack held = player.getStackInHand(hand);
            if (held.getItem() == Items.DRAGON_BREATH) {
                System.out.println("Dragon's breath used");
                player.sendSystemMessage(Text.of("You used a dragon's breath on a beacon"), player.getUuid());
                if (BeaconWarpManager.checkValid(pos, world)) {
                    if (BeaconWarpManager.registerBeacon(pos, world)) {
                        System.out.println("Beacon successfully registered");
                        player.sendSystemMessage(Text.of("Beacon was successfully registered, nice!!"), player.getUuid());
                        world.playSound(null, pos, SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.BLOCKS, 1, 1);
                        if (!player.getAbilities().creativeMode) {
                            held.decrement(1);
                        }
                        BlockPos beaconPos = player.getBlockPos().down();
                        ci.setReturnValue(ActionResult.SUCCESS);
                    } else {
                        player.sendSystemMessage(Text.of("Failed to register beacon, error unknown"), player.getUuid());
                    }
                } else {
                    player.sendSystemMessage(Text.of("This beacon isn't valid"), player.getUuid());
                }
            }
        }
    }

    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BeaconWarpManager.removeBeaconWithLocation(pos, world);
    }
}



