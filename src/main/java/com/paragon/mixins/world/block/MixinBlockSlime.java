package com.paragon.mixins.world.block;

import com.paragon.Paragon;
import com.paragon.impl.event.world.PlayerCollideWithBlockEvent;
import com.paragon.util.world.BlockUtil;
import net.minecraft.block.BlockSlime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockSlime.class)
public class MixinBlockSlime {

    @Inject(method = "onEntityWalk", at = @At("HEAD"), cancellable = true)
    public void hookOnEntityWalk(World worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci) {
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
