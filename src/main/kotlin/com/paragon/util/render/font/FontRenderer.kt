package com.paragon.util.render.font

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ChatAllowedCharacters
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.awt.Font
import kotlin.math.min
import kotlin.random.Random

/**
 * @author Cosmos
 * @author Surge
 */
@SideOnly(Side.CLIENT)
class FontRenderer(font: Font) {

    private val fontHeight: Int
    private val defaultFont: ImageAWT

    init {
        defaultFont = ImageAWT(font)
        fontHeight = height.toInt()
    }

    val height: Float
        get() = defaultFont.getHeight() / 2F

    val size: Int
        get() = defaultFont.font.size

    fun drawStringWithShadow(text: String, x: Float, y: Float, colour: Color): Int {
        return drawString(text, x, y, colour, true)
    }

    fun drawString(text: String, x: Float, y: Float, colour: Color, dropShadow: Boolean): Int {
        val alpha = colour.alpha.coerceAtLeast(5)

        if (text.contains(System.lineSeparator())) {
            val parts = text.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var newY = 0.0f

            for (s in parts) {
                if (dropShadow) {
                    drawText(s, x + 0.6f, y + newY + 0.6f, Color(0, 0, 0, min(alpha, 150)), true)
                }

                drawText(s, x, y + newY, colour, false)
                newY += height
            }

            return 0
        }

        if (dropShadow) {
            drawText(text, x + 0.6f, y + 0.6f, Color(0, 0, 0, min(alpha, 150)), true)
        }

        return drawText(text, x, y, colour, false)
    }

    private fun drawText(text: String?, x: Float, y: Float, colour: Color, ignoreColor: Boolean, ): Int {
        if (text == null) {
            return 0
        }

        if (text.isEmpty()) {
            return x.toInt()
        }

        GlStateManager.translate(x.toDouble(), y.toDouble(), 0.0)
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        var currentColor = colour.rgb

        if (currentColor and -0x4000000 == 0) {
            currentColor = currentColor or -0x1000000
        }

        val alpha = (currentColor shr 24 and 0xFF).coerceAtLeast(5)

        if (text.contains("§")) {
            val parts = text.split("§".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val currentFont = defaultFont
            var width = 0.0
            var randomCase = false

            for (index in parts.indices) {
                val part = parts[index]

                if (part.isEmpty()) {
                    continue
                }

                if (index == 0) {
                    currentFont.drawString(part, width, 0.0, currentColor)
                    width += currentFont.getStringWidth(part).toDouble()
                    continue
                }

                val words = part.substring(1)
                val type = part[0]

                when (val colorIndex = "0123456789abcdefklmnor".indexOf(type)) {
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 -> {
                        if (!ignoreColor) currentColor = ColorUtils.hexColors[colorIndex] or (alpha shl 24)
                        randomCase = false
                    }

                    16 -> randomCase = true
                    18 -> {}
                    21 -> {
                        currentColor = colour.rgb

                        if (currentColor and -0x4000000 == 0) {
                            currentColor = currentColor or -0x1000000
                        }

                        randomCase = false
                    }
                }

                if (randomCase) {
                    currentFont.drawString(ColorUtils.randomMagicText(words), width, 0.0, currentColor)
                } else {
                    currentFont.drawString(words, width, 0.0, currentColor)
                }

                width += currentFont.getStringWidth(words).toDouble()
            }
        } else {
            defaultFont.drawString(text, 0.0, 0.0, currentColor)
        }

        glDisable(GL_LINE_SMOOTH)
        GlStateManager.disableBlend()
        GlStateManager.translate(-x.toDouble(), -y.toDouble(), 0.0)

        return x.toInt()
    }

    fun getStringWidth(text: String): Int {
        if (text.contains("§")) {
            val parts = text.split("§".toRegex()).toTypedArray()
            var width = 0

            for (index in parts.indices) {
                val part = parts[index]

                if (part.isEmpty()) {
                    continue
                }

                if (index == 0) {
                    width += defaultFont.getStringWidth(part)
                    continue
                }

                val words = part.substring(1)
                width += defaultFont.getStringWidth(words)
            }

            return width / 2
        }

        return defaultFont.getStringWidth(text) / 2
    }

    private object ColorUtils {

        private const val magicAllowedCharacters = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■"

        var hexColors = IntArray(16)

        init {
            hexColors[0] = 0
            hexColors[1] = 170
            hexColors[2] = 43520
            hexColors[3] = 43690
            hexColors[4] = 0xAA0000
            hexColors[5] = 0xAA00AA
            hexColors[6] = 0xFFAA00
            hexColors[7] = 0xAAAAAA
            hexColors[8] = 0x555555
            hexColors[9] = 0x5555FF
            hexColors[10] = 0x55FF55
            hexColors[11] = 0x55FFFF
            hexColors[12] = 0xFF5555
            hexColors[13] = 0xFF55FF
            hexColors[14] = 0xFFFF55
            hexColors[15] = 0xFFFFFF
        }

        fun randomMagicText(text: String): String {
            val stringBuilder = StringBuilder()

            for (ch in text.toCharArray()) {
                if (!ChatAllowedCharacters.isAllowedCharacter(ch)) {
                    continue
                }

                val index = Random.nextInt(magicAllowedCharacters.length)
                stringBuilder.append(magicAllowedCharacters[index])
            }

            return stringBuilder.toString()
        }
    }

    companion object {

        fun getColorIndex(type: Char): Int {
            return when (type) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> type.code - 48
                'a', 'b', 'c', 'd', 'e', 'f' -> type.code - 107
                'k', 'l', 'm', 'n', 'o' -> type.code - 123
                'r' -> 21
                else -> -1
            }
        }

    }

}