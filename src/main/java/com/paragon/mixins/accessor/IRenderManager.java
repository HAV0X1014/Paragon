package com.paragon.mixins.accessor;

import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderManager.class)
public interface IRenderManager {

    @Accessor("renderPosX")
    double hookGetRenderPosX();

    @Accessor("renderPosY")
    double hookGetRenderPosY();

    @Accessor("renderPosZ")
    double hookGetRenderPosZ();

}
