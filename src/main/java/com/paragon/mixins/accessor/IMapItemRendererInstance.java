package com.paragon.mixins.accessor;

import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author SooStrator1136
 */
@Mixin(targets = "net.minecraft.client.gui.MapItemRenderer$Instance")
public interface IMapItemRendererInstance {

    @Accessor("mapTexture")
    DynamicTexture hookGetMapTexture();

}
