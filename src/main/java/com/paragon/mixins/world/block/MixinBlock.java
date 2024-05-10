package com.paragon.mixins.world.block;

import com.paragon.Paragon;
import com.paragon.impl.event.render.world.FullCubeBlockEvent;
import com.paragon.impl.event.render.world.SideRenderBlockEvent;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class MixinBlock {

    @Inject(method = "shouldSideBeRendered", at = @At("HEAD"), cancellable = true)
    public void hookShouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side, CallbackInfoReturnable<Boolean> cir) {
        SideRenderBlockEvent sideRenderBlockEvent = new SideRenderBlockEvent(pos);
        Paragon.INSTANCE.getEventBus().post(sideRenderBlockEvent);

        if (sideRenderBlockEvent.isCancelled()) {
            if (sideRenderBlockEvent.getReturnValue()) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }

    @Inject(method = "isFullCube", at = @At("HEAD"), cancellable = true)
    public void hookIsFullCube(IBlockState state, CallbackInfoReturnable<Boolean> cir) {
        FullCubeBlockEvent fullCubeBlockEvent = new FullCubeBlockEvent(state.getBlock());
        Paragon.INSTANCE.getEventBus().post(fullCubeBlockEvent);

        if (fullCubeBlockEvent.isCancelled()) {
            cir.setReturnValue(fullCubeBlockEvent.getReturnValue());
        }
    }

}
