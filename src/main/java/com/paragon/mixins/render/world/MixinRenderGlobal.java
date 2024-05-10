package com.paragon.mixins.render.world;

import com.paragon.Paragon;
import com.paragon.impl.event.render.world.BlockHighlightEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Inject(method = "drawSelectionBox", at = @At("HEAD"), cancellable = true)
    public void hookDrawSelectionBox(EntityPlayer player, RayTraceResult result, int execute, float partialTicks, CallbackInfo ci) {
        BlockHighlightEvent blockHighlightEvent = new BlockHighlightEvent();
        Paragon.INSTANCE.getEventBus().post(blockHighlightEvent);

        if (blockHighlightEvent.isCancelled()) {
            ci.cancel();
        }
    }

}
