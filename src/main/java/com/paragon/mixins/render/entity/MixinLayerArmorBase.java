package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.entity.EnchantColourEvent;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.awt.*;

/**
 * @author Surge
 * @since 19/11/2022
 */
@Mixin(LayerArmorBase.class)
public class MixinLayerArmorBase {

    @ModifyArgs(method = "renderEnchantedGlint", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    private static void hookRenderEnchantedGlint(Args args) {
        EnchantColourEvent event = new EnchantColourEvent(new Color((float) args.get(0), args.get(1), args.get(2), args.get(3)));
        Paragon.INSTANCE.getEventBus().post(event);

        args.setAll(
                event.getColour().getRed() / 255f,
                event.getColour().getGreen() / 255f,
                event.getColour().getBlue() / 255f,
                event.getColour().getAlpha() / 255f
        );
    }

}