package com.paragon.mixins.render.entity;

import com.google.common.base.Predicate;
import com.paragon.Paragon;
import com.paragon.impl.event.player.RaytraceEntityEvent;
import com.paragon.impl.event.render.AspectEvent;
import com.paragon.impl.event.render.RenderWorldEvent;
import com.paragon.impl.event.render.entity.CameraClipEvent;
import com.paragon.impl.event.render.entity.HurtcamEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> hookGetMouseOver(WorldClient world, Entity entity, AxisAlignedBB axisAlignedBB, Predicate<? super Entity> predicate) {
        RaytraceEntityEvent raytraceEvent = new RaytraceEntityEvent();
        Paragon.INSTANCE.getEventBus().post(raytraceEvent);

        if (raytraceEvent.isCancelled()) {
            return new ArrayList<>();
        } else {
            return world.getEntitiesInAABBexcluding(entity, axisAlignedBB, predicate);
        }
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 3)
    public double o3HookOrientCamera(double distance) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(distance);
        Paragon.INSTANCE.getEventBus().post(cameraClipEvent);

        if (cameraClipEvent.isCancelled()) {
            return cameraClipEvent.getDistance();
        }

        return distance;
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 7)
    public double o7HookOrientCamera(double distance) {
        CameraClipEvent cameraClipEvent = new CameraClipEvent(distance);
        Paragon.INSTANCE.getEventBus().post(cameraClipEvent);

        if (cameraClipEvent.isCancelled()) {
            return cameraClipEvent.getDistance();
        }

        return distance;
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hookHurtcameraEffect(float partialTicks, CallbackInfo ci) {
        HurtcamEvent event = new HurtcamEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    public void hookSetupCameraTransform(float fov, float aspect, float z1, float z2) {
        AspectEvent event = new AspectEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            Project.gluPerspective(fov, event.getRatio(), z1, z2);
        } else {
            Project.gluPerspective(fov, (float) Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().displayHeight, z1, z2);
        }
    }

    @Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    public void hookRenderWorldPass(float fov, float aspect, float z1, float z2) {
        AspectEvent event = new AspectEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            Project.gluPerspective(fov, event.getRatio(), z1, z2);
        } else {
            Project.gluPerspective(fov, (float) Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().displayHeight, z1, z2);
        }
    }

    @Redirect(method = "renderCloudsCheck", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/glu/Project;gluPerspective(FFFF)V", remap = false))
    public void hookRenderCloudsCheck(float fov, float aspect, float z1, float z2) {
        AspectEvent event = new AspectEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            Project.gluPerspective(fov, event.getRatio(), z1, z2);
        } else {
            Project.gluPerspective(fov, (float) Minecraft.getMinecraft().displayWidth / Minecraft.getMinecraft().displayHeight, z1, z2);
        }
    }

    @Inject(method = "renderWorld", at = @At("HEAD"), cancellable = true)
    public void hookRenderWorld(float partialTicks, long finishTimeNano, CallbackInfo ci) {
        RenderWorldEvent event = new RenderWorldEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

}
