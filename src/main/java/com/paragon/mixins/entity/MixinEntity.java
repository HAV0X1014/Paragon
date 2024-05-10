package com.paragon.mixins.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.player.StepEvent;
import com.paragon.impl.event.player.WalkOffOfBlockEvent;
import com.paragon.impl.event.world.entity.EntityPushEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {

    public float stepHeight;

    @Shadow
    private AxisAlignedBB boundingBox;

    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void hookApplyEntityCollision(Entity entity, double x, double y, double z) {
        EntityPushEvent event = new EntityPushEvent(entity);
        Paragon.INSTANCE.getEventBus().post(event);

        if (!event.isCancelled()) {
            entity.motionX += x;
            entity.motionY += y;
            entity.motionZ += z;
        }
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = At.Shift.BEFORE, ordinal = 0))
    public void hookMove(MoverType type, double x, double y, double z, CallbackInfo ci) {
        StepEvent stepEvent = new StepEvent(boundingBox, (Entity) (Object) this, 0.5f);
        Paragon.INSTANCE.getEventBus().post(stepEvent);

        if (stepEvent.isCancelled()) {
            stepHeight = stepEvent.getHeight();
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z"))
    public boolean hookMoveIsSneaking(Entity entity) {
        WalkOffOfBlockEvent event = new WalkOffOfBlockEvent(entity);
        Paragon.INSTANCE.getEventBus().post(event);

        return event.isCancelled() || entity.isSneaking();
    }

}
