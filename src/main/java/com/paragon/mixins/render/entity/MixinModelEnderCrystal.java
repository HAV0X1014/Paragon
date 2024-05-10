package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.entity.RenderCrystalEvent;
import net.minecraft.client.model.ModelEnderCrystal;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author rebane2001
 */
@Mixin(ModelEnderCrystal.class)
public class MixinModelEnderCrystal {

    @Shadow
    private ModelRenderer base;

    @Shadow
    @Final
    private ModelRenderer glass;

    @Shadow
    @Final
    private ModelRenderer cube;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void hookRender(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo ci) {
        RenderCrystalEvent renderCrystalEvent = new RenderCrystalEvent(base, glass, cube, (EntityEnderCrystal) entityIn, limbSwingAmount, ageInTicks, scale);
        Paragon.INSTANCE.getEventBus().post(renderCrystalEvent);

        if (renderCrystalEvent.isCancelled()) {
            ci.cancel();
        }
    }

}
