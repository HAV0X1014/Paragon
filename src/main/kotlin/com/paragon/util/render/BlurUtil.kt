package com.paragon.util.render

import com.paragon.mixins.accessor.IShaderGroup
import com.paragon.util.mc
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*

/**
 * @author Surge
 * @since 27/07/22
 */
@SideOnly(Side.CLIENT)
object BlurUtil {

    private var lastScale = -1
    private var lastScaleWidth = -1
    private var lastScaleHeight = -1
    private var framebuffer: Framebuffer? = null
    private var blurShader: ShaderGroup? = null

    private fun checkScale(scaleFactor: Int, widthFactor: Int, heightFactor: Int) {
        if (lastScale != scaleFactor || lastScaleWidth != widthFactor || lastScaleHeight != heightFactor || framebuffer == null || blurShader == null) {
            try {
                blurShader = ShaderGroup(
                    mc.textureManager, mc.resourceManager, mc.framebuffer, ResourceLocation("shaders/post/blur.json")
                )
                blurShader!!.createBindFramebuffers(mc.displayWidth, mc.displayHeight)
                framebuffer = (blurShader as IShaderGroup?)!!.hookGetMainFramebuffer()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        lastScale = scaleFactor
        lastScaleWidth = widthFactor
        lastScaleHeight = heightFactor
    }

    fun blur(x: Float, y: Float, width: Float, height: Float, intensity: Float) {
        val resolution = ScaledResolution(mc)
        val currentScale = resolution.scaleFactor
        checkScale(currentScale, resolution.scaledWidth, resolution.scaledHeight)

        if (OpenGlHelper.isFramebufferEnabled()) {
            RenderUtil.pushScissor(x, y, width, height)

            (blurShader as IShaderGroup?)!!.hookGetListShaders()[0]?.shaderManager?.getShaderUniform("Radius")!!.set(
                intensity
            )

            (blurShader as IShaderGroup?)!!.hookGetListShaders()[1]?.shaderManager?.getShaderUniform("Radius")!!.set(
                intensity
            )

            (blurShader as IShaderGroup?)!!.hookGetListShaders()[0]?.shaderManager?.getShaderUniform("BlurDir")!!.set(
                0f, 1f
            )

            (blurShader as IShaderGroup?)!!.hookGetListShaders()[1]?.shaderManager?.getShaderUniform("BlurDir")!!.set(
                1f, 1f
            )

            framebuffer!!.bindFramebuffer(true)

            blurShader!!.render(mc.renderPartialTicks)

            mc.framebuffer.bindFramebuffer(true)

            RenderUtil.popScissor()

            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ZERO, GL_ONE)

            framebuffer!!.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)

            GlStateManager.disableBlend()
            glScalef(currentScale.toFloat(), currentScale.toFloat(), 0f)
        }
    }

}