package com.paragon.util.render

import com.paragon.util.mc
import com.paragon.util.render.ColourUtil.glColour
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.shader.Shader
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.ARBMultisample.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL20.*
import java.awt.Color
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

@SideOnly(Side.CLIENT)
object RenderUtil {

    private val tessellator = Tessellator.getInstance()
    private val bufferBuilder = tessellator.buffer

    private val roundedRectangleShader = object : Shader("/assets/paragon/glsl/shaders/rounded_rectangle.frag") {
        var width = 0f
        var height = 0f
        var radius = 0f
        var colour = Color(0, 0, 0, 0)

        override fun setupUniforms() {
            setupUniform("size")
            setupUniform("colour")
            setupUniform("alpha")
            setupUniform("radius")
        }

        override fun updateUniforms() {
            glUniform2f(getUniform("size"), width, height)
            glUniform4f(
                getUniform("colour"),
                colour.red / 255f,
                colour.green / 255f,
                colour.blue / 255f,
                colour.alpha / 255f
            )
            glUniform1f(getUniform("radius"), radius)
        }
    }

    private val roundedTextureShader = object : Shader("/assets/paragon/glsl/shaders/rounded_texture.frag") {
        var width = 0f
        var height = 0f
        var radius = 0f
        var alpha = 0f

        override fun setupUniforms() {
            setupUniform("texture")
            setupUniform("size")
            setupUniform("alpha")
            setupUniform("radius")
        }

        override fun updateUniforms() {
            glUniform1i(getUniform("texture"), 0)
            glUniform2f(getUniform("size"), width, height)
            glUniform1f(getUniform("alpha"), alpha / 255f)
            glUniform1f(getUniform("radius"), radius)
        }
    }

    private val roundedOutlineShader = object : Shader("/assets/paragon/glsl/shaders/rounded_outline.frag") {
        var width = 0f
        var height = 0f
        var radius = 0f
        var thickness = 0f
        var colour = Color(0, 0, 0, 0)

        override fun setupUniforms() {
            setupUniform("size")
            setupUniform("colour")
            setupUniform("alpha")
            setupUniform("radius")
            setupUniform("thickness")
        }

        override fun updateUniforms() {
            glUniform2f(getUniform("size"), width, height)
            glUniform4f(
                getUniform("colour"),
                colour.red / 255f,
                colour.green / 255f,
                colour.blue / 255f,
                colour.alpha / 255f
            )
            glUniform1f(getUniform("radius"), radius)
            glUniform1f(getUniform("thickness"), thickness)
        }
    }

    /**
     * Draws a rectangle at the given coordinates
     *
     * @param x The X (left) coordinate
     * @param y The Y (top) coordinate
     * @param width  The width of the rectangle
     * @param height The height of the rectangle
     * @param colour The colour of the rectangle
     */
    fun drawRect(x: Float, y: Float, width: Float, height: Float, colour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        colour.glColour()

        glBegin(GL_QUADS)

        glVertex2f(x, y)
        glVertex2f(x, y + height)
        glVertex2f(x + width, y + height)
        glVertex2f(x + width, y)

        glEnd()

        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a rectangle at the given coordinates, with a gradient going from left to right
     *
     * @param x The X coordinate of the rectangle
     * @param y The Y coordinate of the rectangle
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param leftColour The left colour
     * @param rightColour The colour on the right (what we transition to)
     */
    fun drawHorizontalGradientRect(x: Float, y: Float, width: Float, height: Float, leftColour: Color, rightColour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glShadeModel(GL_SMOOTH)

        glBegin(GL_QUADS)

        leftColour.glColour()

        glVertex2f(x, y)
        glVertex2f(x, y + height)

        rightColour.glColour()

        glVertex2f(x + width, y + height)
        glVertex2f(x + width, y)

        glEnd()

        glShadeModel(GL_FLAT)
        glEnable(GL_ALPHA_TEST)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a rectangle at the given coordinates, with a gradient going from top to bottom
     *
     * @param x The X coordinate of the rectangle
     * @param y The Y coordinate of the rectangle
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param topColour The top colour
     * @param bottomColour The colour on the bottom (what we transition to)
     */
    fun drawVerticalGradientRect(x: Float, y: Float, width: Float, height: Float, topColour: Color, bottomColour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glShadeModel(GL_SMOOTH)

        glBegin(GL_QUADS)

        topColour.glColour()

        glVertex2f(x, y)

        bottomColour.glColour()

        glVertex2f(x, y + height)
        glVertex2f(x + width, y + height)

        topColour.glColour()

        glVertex2f(x + width, y)

        glEnd()

        glShadeModel(GL_FLAT)
        glEnable(GL_ALPHA_TEST)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a triangle at the center of the given coordinates
     *
     * @param x The center X of the triangle
     * @param y The center Y of the triangle
     * @param colour The colour of the triangle
     */
    fun drawTriangle(x: Float, y: Float, width: Float, height: Float, colour: Color) {
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)

        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        glEnable(GL_POLYGON_SMOOTH)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)

        colour.glColour()

        glTranslatef(-(width / 2f), -(height / 2f), 0f)

        glBegin(GL_TRIANGLES)

        glVertex2f(x, y)
        glVertex2f(x, y + height)
        glVertex2f(x + width, y + height / 2)

        glEnd()

        glTranslatef(width / 2f, height / 2f, 0f)

        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_POLYGON_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE)

        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glColor4f(1f, 1f, 1f, 1f)
    }

    /**
     * Draws an arrow at the center of the given coordinates
     *
     * @param x The center X of the arrow
     * @param y The center Y of the arrow
     * @param colour The colour of the arrow
     */
    fun drawArrow(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, colour: Color, angle: Float = 0f) {
        rotate(angle, x, y, 0f) {
            glDisable(GL_DEPTH_TEST)
            glDisable(GL_TEXTURE_2D)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glDepthMask(true)

            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

            glEnable(GL_POLYGON_SMOOTH)
            glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST)

            glLineWidth(lineWidth)

            colour.glColour()

            glTranslatef(-(width / 2f), -(height / 2f), 0f)

            glBegin(GL_LINE_STRIP)

            glVertex2f(x, y)
            glVertex2f(x + width, y + height / 2)
            glVertex2f(x, y + height)

            glEnd()

            glTranslatef(width / 2f, height / 2f, 0f)

            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_POLYGON_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
            glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE)

            glEnable(GL_TEXTURE_2D)
            glEnable(GL_DEPTH_TEST)
            glColor4f(1f, 1f, 1f, 1f)
        }
    }

    /**
     * Draws a rounded rectangle at the given coordinates
     *
     * @param x The X coordinate of the rectangle
     * @param y The Y coordinate of the rectangle
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param radius The radius (corner size) of the rectangle
     * @param colour The colour of the rectangle
     */
    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, colour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        roundedRectangleShader.colour = colour
        roundedRectangleShader.radius = radius
        roundedRectangleShader.width = width
        roundedRectangleShader.height = height

        roundedRectangleShader.startShader()

        glBegin(GL_QUADS)

        glTexCoord2f(0f, 0f)
        glVertex2f(x, y)
        glTexCoord2f(0f, 1f)
        glVertex2f(x, y + height)
        glTexCoord2f(1f, 1f)
        glVertex2f(x + width, y + height)
        glTexCoord2f(1f, 0f)
        glVertex2f(x + width, y)

        glEnd()

        glUseProgram(0)

        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a rounded outline at the given coordinates
     *
     * @param x The X coordinate of the rectangle
     * @param y The Y coordinate of the rectangle
     * @param width The width of the rectangle
     * @param height The height of the rectangle
     * @param radius The radius (corner size) of the rectangle
     * @param thickness How thick the outline is
     * @param colour The colour of the rectangle
     */
    fun drawRoundedOutline(x: Float, y: Float, width: Float, height: Float, radius: Float, thickness: Float, colour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        roundedOutlineShader.colour = colour
        roundedOutlineShader.radius = radius
        roundedOutlineShader.width = width
        roundedOutlineShader.height = height
        roundedOutlineShader.thickness = thickness

        roundedOutlineShader.startShader()

        glBegin(GL_QUADS)

        glTexCoord2f(0f, 0f)
        glVertex2f(x, y)
        glTexCoord2f(0f, 1f)
        glVertex2f(x, y + height)
        glTexCoord2f(1f, 1f)
        glVertex2f(x + width, y + height)
        glTexCoord2f(1f, 0f)
        glVertex2f(x + width, y)

        glEnd()

        glUseProgram(0)

        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a circle at the given coordinates
     *
     * @param x The center X of the circle
     * @param y The center Y of the circle
     * @param radius The radius (corner size) of the circle
     * @param colour The colour of the circle
     */
    fun drawCircle(x: Double, y: Double, radius: Double, colour: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_ALPHA_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glShadeModel(GL_SMOOTH)

        colour.glColour()

        glBegin(GL_TRIANGLE_STRIP)

        var i = 0.0
        while (i < Math.PI * 2) {
            i += Math.PI * 4 / 128

            glVertex2d(x + radius * cos(i), y + radius * sin(i))
            glVertex2d(x, y)
        }

        glEnd()

        glShadeModel(GL_FLAT)
        glEnable(GL_ALPHA_TEST)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Scales whatever is currently being drawn
     *
     * @param x The X to scale from
     * @param y The Y to scale from
     * @param z The Z to scale from
     * @param scaleFacX How much to scale by on the X axis
     * @param scaleFacY How much to scale by on the Y axis
     * @param scaleFacZ How much to scale by on the Z axis
     * @param block The code to run during scaling
     */
    inline fun scaleTo(x: Float, y: Float, z: Float, scaleFacX: Double, scaleFacY: Double, scaleFacZ: Double, block: () -> Unit) {
        glPushMatrix()
        glTranslatef(x, y, z)
        glScaled(scaleFacX, scaleFacY, scaleFacZ)
        glTranslatef(-x, -y, -z)
        block()
        glPopMatrix()
    }

    /**
     * Rotates whatever is currently being drawn
     *
     * @param angle The angle to rotate by
     * @param x The X coordinate of the pivot
     * @param y The Y coordinate of the pivot
     * @param z The Z coordinate of the pivot
     * @param block The code to run during rotation
     */
    inline fun rotate(angle: Float, x: Float, y: Float, z: Float, block: () -> Unit) {
        glPushMatrix()
        glTranslatef(x, y, z)
        glRotated(angle.toDouble(), 0.0, 0.0, 1.0)
        glTranslatef(-x, -y, -z)
        block.invoke()
        glPopMatrix()
    }

    fun drawBorder(x: Float, y: Float, width: Float, height: Float, border: Float, colour: Color) {
        drawRect(x - border, y - border, width + (border * 2f), border, colour)
        drawRect(x - border, y, border, height, colour)
        drawRect(x - border, y + height, width + (border * 2f), border, colour)
        drawRect(x + width, y, border, height, colour)
    }

    /**
     * Starts scissoring a rect
     *
     * @param x The X coordinate of the scissored rect
     * @param y The Y coordinate of the scissored rect
     * @param width The width of the scissored rect
     * @param height The height of the scissored rect
     */
    fun pushScissor(x: Float, y: Float, width: Float, height: Float) {
        var shadowX = x.toInt()
        var shadowY = y.toInt()
        var shadowWidth = width.toInt()
        var shadowHeight = height.toInt()

        shadowWidth = shadowWidth.coerceAtLeast(0)
        shadowHeight = shadowHeight.coerceAtLeast(0)

        glPushAttrib(GL_SCISSOR_BIT)
        run {
            val sr = ScaledResolution(mc)
            val scale = sr.scaleFactor

            shadowY = sr.scaledHeight - shadowY
            shadowX *= scale
            shadowY *= scale
            shadowWidth *= scale
            shadowHeight *= scale

            glScissor(shadowX, (shadowY - shadowHeight), shadowWidth, shadowHeight)
            glEnable(GL_SCISSOR_TEST)
        }
    }

    /**
     * Stops scissoring a rect
     */
    fun popScissor() {
        glDisable(GL_SCISSOR_TEST)
        glPopAttrib()
    }

    /**
     *
     * Draws a gradient box at the given AABB
     * @param axisAlignedBB The AABB to draw the box at
     * @param top The top colour
     * @param bottom The bottom colour
     */
    fun drawGradientBox(axisAlignedBB: AxisAlignedBB, top: Color, bottom: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(1f)

        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        glDisable(GL_CULL_FACE)
        glDisable(GL_ALPHA_TEST)
        glShadeModel(GL_SMOOTH)

        bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        addGradientBoxVertices(axisAlignedBB, bottom, top)
        tessellator.draw()

        glShadeModel(GL_FLAT)
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_CULL_FACE)

        glDisable(GL_LINE_SMOOTH)
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    /**
     * Draws an outline of a gradient box at the given AABB
     *
     * @param axisAlignedBB The AABB to draw the box at
     * @param top The top colour
     * @param bottom The bottom colour
     */
    fun drawOutlineGradientBox(axisAlignedBB: AxisAlignedBB, top: Color, bottom: Color) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(1f)

        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        glDisable(GL_CULL_FACE)
        glDisable(GL_ALPHA_TEST)
        glShadeModel(GL_SMOOTH)

        bufferBuilder.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        addOutlineVertices(axisAlignedBB, bottom, top)
        tessellator.draw()

        glShadeModel(GL_FLAT)
        glEnable(GL_ALPHA_TEST)
        glEnable(GL_CULL_FACE)

        glDisable(GL_LINE_SMOOTH)
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    private fun addGradientBoxVertices(boundingBox: AxisAlignedBB, topColour: Color, bottomColour: Color) {
        val minX = boundingBox.minX
        val minY = boundingBox.minY
        val minZ = boundingBox.minZ
        val maxX = boundingBox.maxX
        val maxY = boundingBox.maxY
        val maxZ = boundingBox.maxZ
        val red = topColour.red / 255f
        val green = topColour.green / 255f
        val blue = topColour.blue / 255f
        val alpha = topColour.alpha / 255f
        val red1 = bottomColour.red / 255f
        val green1 = bottomColour.green / 255f
        val blue1 = bottomColour.blue / 255f
        val alpha1 = bottomColour.alpha / 255f

        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()

        bufferBuilder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()

        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()

        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()

        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()

        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
    }

    /**
     * Adds the gradient outline vertices to the buffer builder
     *
     * @param boundingBox The AABB to add the vertices around
     * @param topColour The top colour
     * @param bottomColour The bottom colour
     */
    private fun addOutlineVertices(boundingBox: AxisAlignedBB, topColour: Color, bottomColour: Color) {
        val minX = boundingBox.minX
        val minY = boundingBox.minY
        val minZ = boundingBox.minZ
        val maxX = boundingBox.maxX
        val maxY = boundingBox.maxY
        val maxZ = boundingBox.maxZ
        val red = topColour.red / 255f
        val green = topColour.green / 255f
        val blue = topColour.blue / 255f
        val alpha = topColour.alpha / 255f
        val red1 = bottomColour.red / 255f
        val green1 = bottomColour.green / 255f
        val blue1 = bottomColour.blue / 255f
        val alpha1 = bottomColour.alpha / 255f

        bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(minX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(minX, minY, maxZ).color(0, 0, 0, 0).endVertex()

        bufferBuilder.pos(maxX, minY, maxZ).color(0, 0, 0, 0).endVertex()
        bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, maxY, maxZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, minY, maxZ).color(0, 0, 0, 0).endVertex()

        bufferBuilder.pos(maxX, minY, minZ).color(0, 0, 0, 0).endVertex()
        bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(maxX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(maxX, minY, minZ).color(0, 0, 0, 0).endVertex()

        bufferBuilder.pos(minX, minY, minZ).color(0, 0, 0, 0).endVertex()
        bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex()
        bufferBuilder.pos(minX, maxY, minZ).color(red1, green1, blue1, alpha1).endVertex()
        bufferBuilder.pos(minX, minY, minZ).color(0, 0, 0, 0).endVertex()
    }

    /**
     * Draws a nametag at a given Vec3d
     *
     * @param text The text to draw
     * @param location Where to draw the text
     * @param textColour The colour of the text
     */
    @JvmStatic
    fun drawNametagText(text: String, location: Vec3d, textColour: Color) {
        GlStateManager.pushMatrix()

        // Translate
        val scale = 0.02666667f

        GlStateManager.translate(location.x - mc.renderManager.viewerPosX, location.y - mc.renderManager.viewerPosY, location.z - mc.renderManager.viewerPosZ)
        GlStateManager.rotate(-mc.player.rotationYaw, 0f, 1f, 0f)

        // Rotate based on the view
        GlStateManager.rotate(mc.player.rotationPitch, if (mc.gameSettings.thirdPersonView == 2) -1f else 1.toFloat(), 0f, 0f)
        GlStateManager.scale(-scale, -scale, scale)
        GlStateManager.disableDepth()
        GlStateManager.translate(-(FontUtil.getStringWidth(text) / 2), 0f, 0f)

        FontUtil.drawStringWithShadow(text, 0f, 0f, textColour)

        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    /**
     * Translates, scales, and rotates around a location
     *
     * @param location The location of the nametag
     * @param scaled Whether the nametag is scaled by distance
     * @param defaultScale The minimum scale of the nametag
     * @param block The code to run when drawing the nametag
     */
    @JvmStatic
    fun drawNametag(location: Vec3d, scaled: Boolean, defaultScale: Double = 0.2, block: () -> Unit) {
        val distance = mc.player.getDistance(location.x, location.y, location.z)

        var scale = defaultScale / 5

        if (scaled) {
            scale = max(defaultScale / 5, distance / 50) / 5
        }

        glPushMatrix()
        RenderHelper.enableStandardItemLighting()
        glDisable(GL_LIGHTING)

        GlStateManager.translate(
            location.x - mc.renderManager.viewerPosX,
            location.y - mc.renderManager.viewerPosY,
            location.z - mc.renderManager.viewerPosZ
        )

        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f)

        GlStateManager.rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)

        // Rotate based on the view
        GlStateManager.rotate(
            (if (mc.gameSettings.thirdPersonView == 2) -1 else 1).toFloat() * mc.player.rotationPitch,
            1f,
            0f,
            0f
        )
        GlStateManager.scale(-scale, -scale, scale)

        glDisable(GL_DEPTH_TEST)

        block.invoke()

        glEnable(GL_DEPTH_TEST)
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        glPopMatrix()
    }

    /**
     * Renders an item stack at the given coordinates
     *
     * @param itemStack The item stack to draw
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param overlay Whether to draw the overlay or not
     */
    @JvmStatic
    fun drawItemStack(itemStack: ItemStack?, x: Float, y: Float, overlay: Boolean) {
        if (itemStack == null) {
            return
        }

        glEnable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
        RenderHelper.enableGUIStandardItemLighting()

        mc.renderItem.zLevel = 0f

        mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x.toInt(), y.toInt())

        if (overlay) {
            mc.renderItem.renderItemOverlays(mc.fontRenderer, itemStack, x.toInt(), y.toInt())
        }

        mc.renderItem.zLevel = 0f

        RenderHelper.disableStandardItemLighting()

        glColor4f(1f, 1f, 1f, 1f)

        glDisable(GL_DEPTH_TEST)
    }

    /**
     * Draws a rectangular texture
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param u The X offset in the texture (for sprite sheets)
     * @param v The Y offset in the texture (for sprite sheets)
     * @param width The width to draw
     * @param height The height to draw
     * @param textureWidth The width of the texture in the sprite sheet
     * @param textureHeight The height of the texture in the sprite sheet
     */
    @JvmStatic
    fun drawModalRectWithCustomSizedTexture(x: Float, y: Float, u: Float, v: Float, width: Float, height: Float, textureWidth: Float, textureHeight: Float) {
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        val f = 1.0f / textureWidth
        val f1 = 1.0f / textureHeight

        bufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX)
        bufferBuilder.pos(x.toDouble(), (y + height).toDouble(), 0.0).tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        bufferBuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        bufferBuilder.pos((x + width).toDouble(), y.toDouble(), 0.0).tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        bufferBuilder.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    /**
     * Draws an entity at the given position
     * @param x The X position to draw the entity at
     * @param y The Y position to draw the entity at
     * @param scale The scale of the entity
     * @param entity The entity to draw
     */
    @JvmStatic
    fun drawEntity(x: Float, y: Float, scale: Float, entity: EntityLivingBase) {
        val yaw = 0f
        val pitch = 0f

        glColor4f(1f, 1f, 1f, 1f)
        glEnable(GL_COLOR_MATERIAL)
        glPushMatrix()
        glTranslatef(x, y, 0f)
        glScalef(-(scale * 30f), scale * 30f, scale * 30f)
        glRotatef(180f, 0f, 0f, 1f)
        glRotatef(135f, 0f, 1f, 0f)

        RenderHelper.enableStandardItemLighting()

        glRotatef(-135f, 0f, 1f, 0f)
        glRotatef(-atan((-pitch / 40.0f).toDouble()).toFloat() * 20.0f, 1f, 0f, 0f)

        val storedRenderYawOffset = entity.renderYawOffset
        val storedRotationYaw = entity.rotationYaw
        val storedRotationPitch = entity.rotationPitch
        val storedRotationYawHead = entity.rotationYawHead
        val storedPrevRotationYawHead = entity.prevRotationYawHead

        entity.renderYawOffset = atan((yaw / 40.0f).toDouble()).toFloat() * 20.0f
        entity.rotationYaw = atan((yaw / 40.0f).toDouble()).toFloat() * 40.0f
        entity.rotationPitch = -atan((pitch / 40.0f).toDouble()).toFloat() * 20.0f
        entity.rotationYawHead = entity.rotationYaw
        entity.prevRotationYawHead = entity.rotationYaw

        mc.renderManager.setPlayerViewY(180f)
        mc.renderManager.isRenderShadow = false

        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        mc.renderManager.renderEntity(entity, 0.0, 0.0, 0.0, 0f, 1f, false)
        glDisable(GL_DEPTH_TEST)

        mc.renderManager.isRenderShadow = true

        entity.renderYawOffset = storedRenderYawOffset
        entity.rotationYaw = storedRotationYaw
        entity.rotationPitch = storedRotationPitch
        entity.rotationYawHead = storedRotationYawHead
        entity.prevRotationYawHead = storedPrevRotationYawHead

        glPopMatrix()

        RenderHelper.disableStandardItemLighting()

        glDisable(GL_RESCALE_NORMAL)
        glActiveTexture(OpenGlHelper.lightmapTexUnit)
        glDisable(GL_TEXTURE_2D)
        glActiveTexture(OpenGlHelper.defaultTexUnit)
    }

}