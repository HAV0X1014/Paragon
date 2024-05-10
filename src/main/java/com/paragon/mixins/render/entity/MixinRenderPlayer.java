package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.entity.RenderArmEvent;
import com.paragon.impl.event.render.entity.RenderNametagEvent;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Shadow
    @Final
    private boolean smallArms;

    @Inject(method = "renderEntityName(Lnet/minecraft/entity/Entity;DDDLjava/lang/String;D)V", at = @At("HEAD"), cancellable = true)
    public void hookRenderEntityName(Entity par1, double par2, double par3, double par4, String par5, double par6, CallbackInfo ci) {
        RenderNametagEvent renderNametagEvent = new RenderNametagEvent(par1);
        Paragon.INSTANCE.getEventBus().post(renderNametagEvent);

        if (renderNametagEvent.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderLeftArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F", opcode = 181))
    public void spHookRenderLeftArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderArmEvent.LeftArmPre renderArmEvent = new RenderArmEvent.LeftArmPre(clientPlayer, this.smallArms);
        Paragon.INSTANCE.getEventBus().post(renderArmEvent);
    }

    @Inject(method = "renderLeftArm", at = @At(value = "RETURN"))
    public void hookRenderLeftArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderArmEvent.LeftArmPost renderArmEvent = new RenderArmEvent.LeftArmPost(clientPlayer, this.smallArms);
        Paragon.INSTANCE.getEventBus().post(renderArmEvent);
    }

    @Inject(method = "renderRightArm", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPlayer;swingProgress:F", opcode = 181))
    public void spHookRenderRightArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderArmEvent.RightArmPre renderArmEvent = new RenderArmEvent.RightArmPre(clientPlayer, this.smallArms);
        Paragon.INSTANCE.getEventBus().post(renderArmEvent);
    }

    @Inject(method = "renderRightArm", at = @At(value = "RETURN"))
    public void hookRenderRightArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        RenderArmEvent.RightArmPost renderArmEvent = new RenderArmEvent.RightArmPost(clientPlayer, this.smallArms);
        Paragon.INSTANCE.getEventBus().post(renderArmEvent);
    }

}
