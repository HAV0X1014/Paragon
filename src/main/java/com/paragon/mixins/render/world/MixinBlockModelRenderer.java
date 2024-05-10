package com.paragon.mixins.render.world;

import com.paragon.Paragon;
import com.paragon.impl.event.render.world.RenderBlockModelEvent;
import com.paragon.impl.event.render.world.RenderBlockSmoothEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

    @Inject(method = "renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z", at = @At("HEAD"), cancellable = true)
    public void hookRenderModel(IBlockAccess iBlockAccess, IBakedModel iBakedModel, IBlockState iBlockState, BlockPos blockPos, BufferBuilder bufferBuilder, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        RenderBlockModelEvent renderBlockModelEvent = new RenderBlockModelEvent(blockPos);
        Paragon.INSTANCE.getEventBus().post(renderBlockModelEvent);

        if (renderBlockModelEvent.isCancelled()) {
            if (renderBlockModelEvent.getReturnValue()) {
                callbackInfoReturnable.setReturnValue(false);
                callbackInfoReturnable.cancel();
            }
        }
    }

    @Inject(method = "renderModelSmooth", at = @At("HEAD"), cancellable = true)
    public void hookRenderModelSmooth(IBlockAccess iBlockAccess, IBakedModel iBakedModel, IBlockState iBlockState, BlockPos blockPos, BufferBuilder bufferBuilder, boolean checkSides, long rand, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        RenderBlockSmoothEvent renderBlockModelEvent = new RenderBlockSmoothEvent(blockPos);
        Paragon.INSTANCE.getEventBus().post(renderBlockModelEvent);

        if (renderBlockModelEvent.isCancelled()) {
            if (renderBlockModelEvent.getReturnValue()) {
                callbackInfoReturnable.setReturnValue(false);
                callbackInfoReturnable.cancel();
            }
        }
    }

}
