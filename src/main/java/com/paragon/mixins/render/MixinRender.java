package com.paragon.mixins.render;

import com.paragon.Paragon;
import com.paragon.impl.event.render.ShaderColourEvent;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Render.class)
public class MixinRender<T extends Entity> {

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    public void hookGetTeamColor(T entity, CallbackInfoReturnable<Integer> info) {
        ShaderColourEvent event = new ShaderColourEvent(entity);
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            info.cancel();
            info.setReturnValue(event.getColour().getRGB());
        }
    }

}
