package com.paragon.mixins.render.entity;

import com.paragon.Paragon;
import com.paragon.impl.event.render.entity.EntityHighlightOnHitEvent;
import com.paragon.impl.event.render.entity.RenderEntityEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.FloatBuffer;

@Mixin(value = RenderLivingBase.class, priority = Integer.MAX_VALUE)
public abstract class MixinRenderLivingBase<T extends EntityLivingBase> extends Render<T> {

    @Shadow
    protected ModelBase mainModel;

    @Shadow
    protected abstract int getColorMultiplier(T entitylivingbaseIn, float lightBrightness, float partialTickTime);

    @Shadow
    protected FloatBuffer brightnessBuffer;

    @Shadow
    @Final
    private static DynamicTexture TEXTURE_BRIGHTNESS;

    protected MixinRenderLivingBase(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "renderModel", at = @At("TAIL"), cancellable = true)
    public void hookRenderModel(T entitylivingbaseIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, CallbackInfo ci) {
        RenderEntityEvent renderEntityEvent = new RenderEntityEvent(mainModel, entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
        Paragon.INSTANCE.getEventBus().post(renderEntityEvent);

        if (renderEntityEvent.isCancelled()) {
            ci.cancel();
        }
    }

    /**
     * Couldn't figure out how to inject into the if statement, so here we are.
     */
    @Inject(method = "setBrightness", at = @At("HEAD"), cancellable = true)
    public void hookSetBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {
        EntityHighlightOnHitEvent event = new EntityHighlightOnHitEvent();
        Paragon.INSTANCE.getEventBus().post(event);

        if (event.isCancelled()) {
            cir.cancel();

            float f = entitylivingbaseIn.getBrightness();
            int i = getColorMultiplier(entitylivingbaseIn, f, partialTicks);
            boolean flag = (i >> 24 & 255) > 0;
            boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;

            if (! flag && ! flag1) {
                cir.setReturnValue(false);
            } else if (! flag && ! combineTextures) {
                cir.setReturnValue(false);
            } else {
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
                GlStateManager.enableTexture2D();
                GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.defaultTexUnit);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PRIMARY_COLOR);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.defaultTexUnit);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.enableTexture2D();
                GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, OpenGlHelper.GL_INTERPOLATE);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_CONSTANT);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.GL_PREVIOUS);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE2_RGB, OpenGlHelper.GL_CONSTANT);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND2_RGB, 770);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);

                this.brightnessBuffer.position(0);
                if (flag1) {
                    this.brightnessBuffer.put(event.getColour().getRed() / 255f);
                    this.brightnessBuffer.put(event.getColour().getGreen() / 255f);
                    this.brightnessBuffer.put(event.getColour().getBlue() / 255f);
                    this.brightnessBuffer.put(event.getColour().getAlpha() / 255f);
                } else {
                    float f1 = (float) (i >> 24 & 255) / 255.0F;
                    float f2 = (float) (i >> 16 & 255) / 255.0F;
                    float f3 = (float) (i >> 8 & 255) / 255.0F;
                    float f4 = (float) (i & 255) / 255.0F;
                    this.brightnessBuffer.put(f2);
                    this.brightnessBuffer.put(f3);
                    this.brightnessBuffer.put(f4);
                    this.brightnessBuffer.put(1.0F - f1);
                }

                this.brightnessBuffer.flip();
                GlStateManager.glTexEnv(8960, 8705, this.brightnessBuffer);
                GlStateManager.setActiveTexture(OpenGlHelper.GL_TEXTURE2);
                GlStateManager.enableTexture2D();
                GlStateManager.bindTexture(TEXTURE_BRIGHTNESS.getGlTextureId());
                GlStateManager.glTexEnvi(8960, 8704, OpenGlHelper.GL_COMBINE);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_RGB, 8448);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_RGB, OpenGlHelper.GL_PREVIOUS);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE1_RGB, OpenGlHelper.lightmapTexUnit);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_RGB, 768);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND1_RGB, 768);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_COMBINE_ALPHA, 7681);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_SOURCE0_ALPHA, OpenGlHelper.GL_PREVIOUS);
                GlStateManager.glTexEnvi(8960, OpenGlHelper.GL_OPERAND0_ALPHA, 770);
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

                cir.setReturnValue(true);
            }
        }
    }

}
