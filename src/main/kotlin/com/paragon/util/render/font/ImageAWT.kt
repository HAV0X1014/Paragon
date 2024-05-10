package com.paragon.util.render.font

import com.paragon.util.render.ColourUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager.bindTexture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage

/**
 * @author Surge
 * @since 27/08/2022
 */
@SideOnly(Side.CLIENT)
class ImageAWT(val font: Font, startChar: Int, stopChar: Int) {

    constructor(font: Font) : this(font, 0, 255)

    private val activeFontRenderers: ArrayList<ImageAWT> = arrayListOf()
    private var gcTicks: Int = 0
    private val charLocations: Array<CharLocation?> = arrayOfNulls(stopChar)
    private val cachedStrings: HashMap<String, FontCache> = hashMapOf()
    private var fontHeight = -1
    private var textureID = 0
    private var textureWidth = 0
    private var textureHeight = 0

    init {
        renderBitmap(startChar, stopChar)
        activeFontRenderers.add(this)
    }

    fun drawString(text: String, x: Double, y: Double, colour: Int) {
        gcTick()

        glPushMatrix()
        glEnable(GL_BLEND)
        glScaled(0.25, 0.25, 0.25)
        glTranslated(x * 2.0, y * 2.0, 0.0)

        bindTexture(textureID)

        ColourUtil.setColour(colour)

        var currX = 0.0
        val cached = cachedStrings[text]

        if (cached != null) {
            glCallList(cached.displayList)
            cached.lastUsage = System.currentTimeMillis()

            glPopMatrix()
            return
        }

        glBegin(GL_QUADS)
        text.toCharArray().forEach {
            if (Character.getNumericValue(it) >= charLocations.size) {
                glEnd()

                glScaled(4.0, 4.0, 4.0)

                Minecraft.getMinecraft().fontRenderer.drawString(it.toString(), (currX * 0.25f).toFloat(), 0.0f, colour, false)

                currX += Minecraft.getMinecraft().fontRenderer.getStringWidth(it.toString()) * 4.0

                glScaled(0.25, 0.25, 0.25)

                bindTexture(textureID)

                ColourUtil.setColour(colour)

                glBegin(GL_QUADS)
                return@forEach
            }

            val fontChar = try {
                charLocations[it.code]
            } catch (throwable: Throwable) {
                charLocations[0]
            }

            if (charLocations.size <= it.code || fontChar == null) {
                return@forEach
            }

            drawChar(fontChar, currX, 0.0)

            currX += fontChar.width - 8.0
        }

        glEnd()

        glPopMatrix()
    }

    fun getStringWidth(text: String): Int {
        var width = 0
        text.toCharArray().forEach {
            val index = if (it.code < charLocations.size) it.code else 3

            val fontChar = charLocations[index]

            if (charLocations.size <= index || fontChar == null) {
                width += Minecraft.getMinecraft().fontRenderer.getStringWidth(it.toString()) / 4
                return@forEach
            }

            width += fontChar.width - 8
        }

        return width / 2
    }

    fun getHeight(): Float {
        return (fontHeight - 8f) / 2f
    }

    private fun drawChar(char: CharLocation, x: Double, y: Double) {
        val width: Float = char.width.toFloat()
        val height: Float = char.height.toFloat()
        val srcX: Float = char.x.toFloat()
        val srcY: Float = char.y.toFloat()
        val renderX = srcX / textureWidth.toFloat()
        val renderY = srcY / textureHeight.toFloat()
        val renderWidth = width / textureWidth.toFloat()
        val renderHeight = height / textureHeight.toFloat()

        glTexCoord2f(renderX, renderY)
        glVertex2f(x.toFloat(), y.toFloat())
        glTexCoord2f(renderX, renderY + renderHeight)
        glVertex2f(x.toFloat(), (y + height).toFloat())
        glTexCoord2f(renderX + renderWidth, renderY + renderHeight)
        glVertex2f((x + width).toFloat(), (y + height).toFloat())
        glTexCoord2f(renderX + renderWidth, renderY)
        glVertex2f((x + width).toFloat(), y.toFloat())
    }

    fun gcTick() {
        if (gcTicks++ > 600) {
            activeFontRenderers.forEach { it.collectGarbage() }
            gcTicks = 0
        }
    }

    fun collectGarbage() {
        val currentTime = System.currentTimeMillis()
        cachedStrings.filter { entry ->
            currentTime - entry.value.lastUsage > 30000L
        }.forEach { entry ->
            glDeleteLists(entry.value.displayList, 1)
            cachedStrings.remove(entry.key)
        }
    }

    private fun renderBitmap(startChar: Int, endChar: Int) {
        val fontImages = arrayOfNulls<BufferedImage>(endChar)
        var rowHeight = 0
        var charX = 0
        var charY = 0

        for (targetChar in startChar..endChar) {
            val fontImage = drawCharToImage(targetChar.toChar())
            val fontChar = CharLocation(charX, charY, fontImage.width, fontImage.height)

            if (fontChar.height > fontHeight) {
                fontHeight = fontChar.height
            }

            if (fontChar.height > rowHeight) {
                rowHeight = fontChar.height
            }

            if (charLocations.size <= targetChar) {
                continue
            }

            charLocations[targetChar] = fontChar
            fontImages[targetChar] = fontImage

            charX += fontChar.width

            if (charX <= 2048) {
                continue
            }

            if (charX > textureWidth) {
                textureWidth = charX
            }

            charX = 0
            charY += rowHeight
            rowHeight = 0
        }

        textureHeight = charY + rowHeight
        val bufferedImage = BufferedImage(textureWidth, textureHeight, 2)
        val graphics2D = bufferedImage.graphics as Graphics2D
        graphics2D.font = font
        graphics2D.color = Color(255, 255, 255, 0)
        graphics2D.fillRect(0, 0, textureWidth, textureHeight)
        graphics2D.color = Color.WHITE

        for (targetChar in startChar..endChar) {
            if (fontImages.size <= targetChar || charLocations.size <= targetChar || fontImages[targetChar] == null || charLocations[targetChar] == null) {
                continue
            }

            graphics2D.drawImage(fontImages[targetChar], charLocations[targetChar]!!.x, charLocations[targetChar]!!.y, null)
        }

        textureID = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), bufferedImage, true, true)
    }

    private fun drawCharToImage(ch: Char): BufferedImage {
        val graphics2D = BufferedImage(1, 1, 2).graphics as Graphics2D
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics2D.font = font

        val metrics = graphics2D.fontMetrics

        var charWidth = metrics.charWidth(ch) + 8

        if (charWidth <= 8) {
            charWidth = 7
        }

        var charHeight: Int = metrics.height + 3

        if (charHeight <= 0) {
            charHeight = font.size
        }

        val fontImage = BufferedImage(charWidth, charHeight, 2)

        val graphics = fontImage.graphics as Graphics2D

        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.font = font
        graphics.color = Color.WHITE
        graphics.drawString(ch.toString(), 0, metrics.ascent)

        return fontImage
    }

    class CharLocation(val x: Int, val y: Int, val width: Int, val height: Int)
}