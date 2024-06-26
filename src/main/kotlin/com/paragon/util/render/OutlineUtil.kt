package com.paragon.util.render

import com.paragon.util.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * @author Hexeption
 */
object OutlineUtil {

    @JvmStatic
    fun renderOne(lineWidth: Float) {
        checkSetupFBO()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_NEVER, 1, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    @JvmStatic
    fun renderTwo() {
        glStencilFunc(GL_NEVER, 0, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    @JvmStatic
    fun renderThree() {
        glStencilFunc(GL_EQUAL, 1, 0xF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    @JvmStatic
    fun renderFour(c: Color) {
        setColor(c)
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1.0f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
    }

    @JvmStatic
    fun renderFive() {
        glPolygonOffset(1.0f, 2000000f)
        glDisable(GL_POLYGON_OFFSET_LINE)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glEnable(GL_BLEND)
        glEnable(GL_LIGHTING)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_ALPHA_TEST)
        glPopAttrib()
    }

    private fun setColor(c: Color) {
        glColor4d(
            (c.red / 255f).toDouble(),
            (c.green / 255f).toDouble(),
            (c.blue / 255f).toDouble(),
            (c.alpha / 255f).toDouble()
        )
    }

    private fun checkSetupFBO() {
        // Gets the FBO of Minecraft
        val fbo = Minecraft.getMinecraft().framebuffer

        // Check if FBO isn't null
        // Checks if screen has been resized or new FBO has been created
        if (fbo.depthBuffer > -1) {
            // Sets up the FBO with depth and stencil extensions (24/8 bit)
            setupFBO(fbo)
            // Reset the ID to prevent multiple FBO's
            fbo.depthBuffer = -1
        }
    }

    /**
     * Sets up the FBO with depth and stencil
     *
     * @param fbo FrameBuffer
     */
    private fun setupFBO(fbo: Framebuffer) {
        // Deletes old render buffer extensions such as depth
        // Args: Render Buffer ID
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)

        // Generates a new render buffer ID for the depth and stencil extension
        val stencilDepthBufferId = EXTFramebufferObject.glGenRenderbuffersEXT()

        // Binds new render buffer by ID
        // Args: Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferId)

        // Adds the depth and stencil extension
        // Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT),
        // Width, Height
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth,
            mc.displayHeight
        )

        // Adds the stencil attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferId
        )

        // Adds the depth attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencilDepthBufferId
        )
    }
}