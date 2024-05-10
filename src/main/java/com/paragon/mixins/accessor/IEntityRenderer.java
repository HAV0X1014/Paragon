package com.paragon.mixins.accessor;

import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface IEntityRenderer {

    @Invoker("setupCameraTransform")
    void hookSetupCameraTransform(float partialTicks, int pass);

    @Accessor("mapItemRenderer")
    MapItemRenderer getMapItemRenderer();

}
