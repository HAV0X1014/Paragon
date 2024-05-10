package com.paragon.mixins.world.block;

import com.paragon.Paragon;
import com.paragon.impl.event.world.PlayerCollideWithBlockEvent;
import com.paragon.util.world.BlockUtil;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSoulSand.class)
public class MixinBlockSoulSand {

    @Inject(method = "onEntityCollidedWithBlock", at = @At("HEAD"), cancellable = true)
    public void hookOnEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn, CallbackInfo ci) {
        try {
            if (entityIn.getEntityId() == Minecraft.getMinecraft().player.getEntityId()) {
                PlayerCollideWithBlockEvent blockSlowEvent = new PlayerCollideWithBlockEvent(pos, BlockUtil.getBlockAtPos(pos));
                Paragon.INSTANCE.getEventBus().post(blockSlowEvent);

                if (blockSlowEvent.isCancelled()) {
                    ci.cancel();
                }
            }
        }
        // Why does this throw an NPE
        catch (NullPointerException exception) {
            exception.printStackTrace();
        }
    }

}