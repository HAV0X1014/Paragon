package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.player.RenderItemEvent;
import com.paragon.impl.event.render.entity.RenderEatingEvent;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @Inject(method = "transformEatFirstPerson", at = @At("HEAD"), cancellable = true)
    public void hookTransformEatFirstPerson(float a, EnumHandSide side, ItemStack stack, CallbackInfo ci) {
        RenderEatingEvent renderEatingEvent = new RenderEatingEvent();
        Paragon.INSTANCE.getEventBus().post(renderEatingEvent);

        if (renderEatingEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "transformFirstPerson", at = @At("HEAD"))
    public void headHookTransformFirstPerson(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        Paragon.INSTANCE.getEventBus().post(new RenderItemEvent.Pre(hand));
    }

    @Inject(method = "transformFirstPerson", at = @At("TAIL"))
    public void tailHookTransformFirstPerson(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        Paragon.INSTANCE.getEventBus().post(new RenderItemEvent.Post(hand));
    }

    @Inject(method = "transformSideFirstPerson", at = @At("HEAD"))
    public void hookTransformSideFirstPerson(EnumHandSide hand, float p_187453_2_, CallbackInfo ci) {
        Paragon.INSTANCE.getEventBus().post(new RenderItemEvent.Pre(hand));
    }

}
