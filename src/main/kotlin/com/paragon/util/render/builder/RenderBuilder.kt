package com.paragon.util.render.builder

import com.paragon.util.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * @author Surge
 * @since 20/08/2022
 */
class RenderBuilder {

    private var depth = false
    private var blend = false
    private var texture = false
    private var alpha = false

    private var boundingBox = AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)

    private var renderMode = BoxRenderMode.BOTH

    private var innerColour = Color.BLACK
    private var outerColour = Color.BLACK

    fun start(): RenderBuilder {
        glPushMatrix()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        return this
    }

    fun build(offset: Boolean) {
        if (offset) {
            boundingBox = boundingBox.offset(
                -mc.renderManager.viewerPosX,
                -mc.renderManager.viewerPosY,
                -mc.renderManager.viewerPosZ
            )
        }

        if (renderMode == BoxRenderMode.FILL || renderMode == BoxRenderMode.BOTH) {
            RenderGlobal.renderFilledBox(
                boundingBox,
                innerColour.red / 255f,
                innerColour.green / 255f,
                innerColour.blue / 255f,
                innerColour.alpha / 255f
            )
        }

        if (renderMode == BoxRenderMode.OUTLINE || renderMode == BoxRenderMode.BOTH) {
            RenderGlobal.drawSelectionBoundingBox(
                boundingBox,
                outerColour.red / 255f,
                outerColour.green / 255f,
                outerColour.blue / 255f,
                outerColour.alpha / 255f
            )
        }

        if (depth) {
            glDepthMask(true)
            glEnable(GL_DEPTH_TEST)
        }

        if (blend) {
            glEnable(GL_BLEND)
        }

        if (alpha) {
            glEnable(GL_ALPHA_TEST)
        }

        if (texture) {
            glEnable(GL_TEXTURE_2D)
        }

        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun boundingBox(boundingBoxIn: AxisAlignedBB): RenderBuilder {
        this.boundingBox = boundingBoxIn

        return this
    }

    fun type(typeIn: BoxRenderMode): RenderBuilder {
        renderMode = typeIn

        return this
    }

    fun depth(depthIn: Boolean): RenderBuilder {
        if (depthIn) {
            glDisable(GL_DEPTH_TEST)
            glDepthMask(false)
        }

        depth = depthIn
        return this
    }

    fun blend(blendIn: Boolean): RenderBuilder {
        if (blendIn) {
            glEnable(GL_BLEND)
        }

        blend = blendIn
        return this
    }

    fun texture(textureIn: Boolean): RenderBuilder {
        if (textureIn) {
            glDisable(GL_TEXTURE_2D)
        }

        texture = textureIn
        return this
    }

    fun alpha(alphaIn: Boolean): RenderBuilder {
        if (alphaIn) {
            glDisable(GL_ALPHA_TEST)
        }

        alpha = alphaIn
        return this
    }

    fun lineWidth(lineWidthIn: Float): RenderBuilder {
        glLineWidth(lineWidthIn)
        return this
    }

    fun inner(colour: Color): RenderBuilder {
        innerColour = colour

        return this
    }

    fun outer(colour: Color): RenderBuilder {
        outerColour = colour

        return this
    }

}