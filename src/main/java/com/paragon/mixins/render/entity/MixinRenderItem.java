package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.entity.EnchantColourEvent;
import net.minecraft.client.renderer.RenderItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.awt.*;

/**
 * @author Surge
 * @since 19/11/2022
 */
@Mixin(RenderItem.class)
public class MixinRenderItem {

    @ModifyArg(method = "renderEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderItem;renderModel(Lnet/minecraft/client/renderer/block/model/IBakedModel;I)V"))
    public int hookRenderEffect(int colour) {
        EnchantColourEvent event = new EnchantColourEvent(new Color(colour));
        Paragon.INSTANCE.getEventBus().post(event);

        return event.getColour().getRGB();
    }

}
