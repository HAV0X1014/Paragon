package com.paragon.impl.ui.configuration.retrowindows.element.setting.elements

import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.impl.ui.configuration.retrowindows.element.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

/**
 * @author Surge
 */
class ColourElement(parent: ModuleElement, setting: Setting<Color>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Color>(parent, setting, x, y, width, height) {

    private val hueSetting = Setting("Hue", 0f, 0f, 360f, 1f) as Setting<Number>
    private val alphaSetting = Setting("Alpha", 0f, 0f, 255f, 1f) as Setting<Number>
    private val rainbowSetting = Setting("Rainbow", false)
    private val rainbowSpeedSetting = Setting("Speed", 4f, 0.1f, 10f, 0.1f) as Setting<Number>
    private val rainbowSaturationSetting = Setting("Saturation", 100f, 0f, 100f, 1f) as Setting<Number>
    private val syncSetting = Setting("Sync", false)

    private var pickingColour = false
    private var finalColour = Color(0, 0, 0)

    init {
        val values = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

        hueSetting.setValue(((values[0] * 360f).toInt()).toFloat())
        alphaSetting.setValue(setting.value.alpha.toFloat())
        rainbowSetting.setValue(setting.isRainbow)
        rainbowSpeedSetting.setValue(setting.rainbowSpeed)
        rainbowSaturationSetting.setValue(setting.rainbowSaturation)
        syncSetting.setValue(setting.isSync)

        subSettings.addAll(
            listOf(
                SliderElement(parent, hueSetting, x + 2, y, width - 4, height), SliderElement(parent, alphaSetting, x + 2, y, width - 4, height), BooleanElement(parent, rainbowSetting, x + 2, y, width - 4, height), SliderElement(parent, rainbowSpeedSetting, x + 2, y, width - 4, height), SliderElement(parent, rainbowSaturationSetting, x + 2, y, width - 4, height), BooleanElement(parent, syncSetting, x + 2, y, width - 4, height)
            )
        )

        finalColour = setting.value
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x + 3, y + 3, width - 4, height - 4, Color(100, 100, 100))
        RenderUtil.drawRect(x + 2, y + 2, width - 4, height - 4, Color(130, 130, 130))

        RenderUtil.drawHorizontalGradientRect(x + 2, y + 2, ((width - 4) * expanded.getAnimationFactor()).toFloat(), height - 4, setting.value, if (ClickGUI.gradient.value) setting.value.brighter().brighter() else setting.value)

        glScalef(0.8f, 0.8f, 0.8f)

        val scaleFactor = 1 / 0.8f
        FontUtil.drawStringWithShadow(setting.name, (x + 5) * scaleFactor, (y + 5f) * scaleFactor, Color.WHITE)

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        if (expanded.getAnimationFactor() > 0) {
            val pickerWidth = 76f
            val xPicker = (x + (width / 2f)) - (pickerWidth / 2)

            val colour = Color.getHSBColor(hueSetting.value.toFloat() / 360f, 1f, 1f)

            var yOffset = pickerWidth + 4

            val scissorY = MathHelper.clamp(y, parent.parent.y + parent.parent.height, (parent.parent.y + parent.parent.height + parent.parent.scissorHeight) - getTotalHeight())

            RenderUtil.pushScissor(x, scissorY, width, getTotalHeight())

            drawPicker(xPicker, y + height + 2, pickerWidth, colour)

            // Check we are dragging
            if (pickingColour) {
                val saturation: Float
                val brightness: Float

                val satDiff = min(pickerWidth, max(0f, mouseX - xPicker))

                saturation = if (satDiff == 0f) {
                    0f
                }
                else {
                    MathsUtil.roundDouble((satDiff / pickerWidth * 100).toDouble(), 0).toFloat()
                }

                val brightDiff = min(pickerWidth, max(0f, (y + height + 2 + pickerWidth) - mouseY))

                brightness = if (brightDiff == 0f) {
                    0f
                }
                else {
                    MathsUtil.roundDouble((brightDiff / pickerWidth * 100).toDouble(), 0).toFloat()
                }

                finalColour = Color(Color.HSBtoRGB(hueSetting.value.toFloat() / 360, saturation / 100, brightness / 100)).integrateAlpha(alphaSetting.value.toFloat())
            }

            subSettings.forEach {
                if (it is SliderElement && it.dragging) {
                    val hsb = Color.RGBtoHSB(finalColour.red, finalColour.green, finalColour.blue, null)
                    finalColour = Color(Color.HSBtoRGB(hueSetting.value.toInt() / 360f, hsb[1], hsb[2]))

                    pickingColour = false
                }
            }

            // Get final HSB colours
            val finHSB = Color.RGBtoHSB(finalColour.red, finalColour.green, finalColour.blue, null)

            // Picker X and Y
            val pickerX: Float = xPicker + finHSB[1] * pickerWidth
            val pickerY: Float = y + height + 2 + (1 - finHSB[2]) * pickerWidth

            // Draw picker highlight
            RenderUtil.drawRect(pickerX - 1.5f, pickerY - 1.5f, 3f, 3f, Color.WHITE)
            RenderUtil.drawRect(pickerX - 1, pickerY - 1, 2f, 2f, finalColour)

            subSettings.forEach {
                it.x = x + 2
                it.y = y + height + yOffset

                it.draw(mouseX, mouseY, mouseDelta)

                yOffset += it.getTotalHeight()
            }

            RenderUtil.popScissor()
        }

        setting.isSync = syncSetting.value
        setting.isRainbow = rainbowSetting.value
        setting.rainbowSpeed = rainbowSpeedSetting.value.toFloat()
        setting.rainbowSaturation = rainbowSaturationSetting.value.toFloat()

        setting.setValue(finalColour)
    }

    private fun drawPicker(x: Float, y: Float, dimension: Float, colour: Color) {
        // GL shit pt 1
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
        GlStateManager.shadeModel(GL_SMOOTH)

        // Get tessellator and buffer builder
        val tessellator = Tessellator.getInstance()
        val bufferbuilder = tessellator.buffer

        // Add vertexes
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR)
        bufferbuilder.pos((x + dimension).toDouble(), y.toDouble(), 0.0).color(colour.red, colour.green, colour.blue, colour.alpha).endVertex()
        bufferbuilder.pos(x.toDouble(), y.toDouble(), 0.0).color(255, 255, 255, 255).endVertex()
        bufferbuilder.pos(x.toDouble(), (y + dimension).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()
        bufferbuilder.pos((x + dimension).toDouble(), (y + dimension).toDouble(), 0.0).color(0, 0, 0, 255).endVertex()

        // Draw rect
        tessellator.draw()

        // GL shit pt 2
        GlStateManager.shadeModel(GL_FLAT)
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()

        RenderUtil.drawBorder(x, y, dimension, dimension, 0.5f, Color.WHITE)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (click == Click.LEFT && expanded.state) {
            if (mouseX in (x + (width / 2f)) - (76 / 2)..(x + (width / 2f)) + (76 / 2) && mouseY in y + height + 2..y + height + 78) {
                pickingColour = true
            }
        }

        if (isHovered(mouseX, mouseY) && y in parent.parent.y + parent.parent.height..parent.parent.y + parent.parent.height + parent.parent.scissorHeight) {
            if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        pickingColour = false
    }

    override fun getSubSettingHeight(): Float {
        var height = 80f

        subSettings.forEach {
            height += it.getTotalHeight()
        }

        return height
    }

}