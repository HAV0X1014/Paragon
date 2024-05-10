package com.paragon.mixins.render.tileentity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.tileentity.RenderTileEntityEvent;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {

    @Shadow
    public static TileEntityRendererDispatcher instance;

    @Shadow
    public static double staticPlayerX;

    @Shadow
    public static double staticPlayerY;

    @Shadow
    public static double staticPlayerZ;

    @Inject(method = "render(Lnet/minecraft/tileentity/TileEntity;FI)V", at = @At("HEAD"))
    public void hookRender(TileEntity tileEntity, float partialTicks, int destroyStage, CallbackInfo ci) {
        RenderTileEntityEvent renderTileEntityEvent = new RenderTileEntityEvent(this.instance, tileEntity, partialTicks, staticPlayerX, staticPlayerY, staticPlayerZ);
        Paragon.INSTANCE.getEventBus().post(renderTileEntityEvent);
    }

}
