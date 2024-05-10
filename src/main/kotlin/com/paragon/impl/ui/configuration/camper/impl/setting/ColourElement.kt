package com.paragon.impl.ui.configuration.camper.impl.setting

import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.camper.impl.ModuleElement
import com.paragon.impl.ui.configuration.camper.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.render.ColourUtil.glColour
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.ColourUtil.toColour
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.math.BigDecimal
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class ColourElement(parent: ModuleElement, setting: Setting<Color>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Color>(parent, setting, x, y, width, height) {

    private val picker = Picker(x, y, width - 8f, width - 8f)
    private val hueSlider = HueSlider(x, y, width - 8f, 10f)
    private val alphaSlider = AlphaSlider(x, y, width - 8f, 10f)

    private val rainbow = Button(
        "Rainbow",
        {
            setting.isRainbow = !setting.isRainbow
        },
        {
           setting.isRainbow
        },
        x, y, (width - 8f) / 2f)

    private val sync = Button(
        "Sync",
        {
            setting.isSync = !setting.isSync
        },
        {
            setting.isSync
        },
        x, y, (width - 8f) / 2f)

    init {
        val hsb = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

        hueSlider.hue = hsb[0] * 360
        picker.saturation = hsb[1]
        picker.brightness = hsb[2]

        alphaSlider.alpha = (255 - setting.value.alpha).toFloat()
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        val hsbValues = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

        if (hsbValues[0] * 360f != hueSlider.hue) {
            hueSlider.hue = hsbValues[0] * 360
        }

        RenderUtil.drawRect(x, y, width, height, hover.getColour())

        RenderUtil.scaleTo(x + 3, y + 5.5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 3f, Color.WHITE)
        }

        var colour = setting.value

        if (expanded.getAnimationFactor() > 0) {
            val factor = expanded.getAnimationFactor()

            if (factor != 1.0 && factor != 0.0) {
                RenderUtil.pushScissor(
                    x,
                    MathHelper.clamp(y + 4f, parent.parent.y + parent.parent.height, 100000f),
                    width,
                    MathHelper.clamp(getAbsoluteHeight() - 4, 0f, (parent.parent.y + parent.parent.height + parent.parent.moduleHeight).toFloat() - (y + 4f))
                )
            }

            RenderUtil.drawRect(x, y + height, width, getAbsoluteHeight() - height, Color(0, 0, 0, 150))

            var xOffset = x + 4f
            var yOffset = y + height + 4f

            // position elements
            picker.x = xOffset
            picker.y = yOffset
            yOffset += picker.height + 3

            hueSlider.x = xOffset
            hueSlider.y = yOffset
            yOffset += hueSlider.height + 3

            alphaSlider.x = xOffset
            alphaSlider.y = yOffset
            alphaSlider.width = width - 8
            yOffset += alphaSlider.height + 3

            rainbow.x = xOffset
            rainbow.y = yOffset
            xOffset += rainbow.width

            sync.x = xOffset
            sync.y = yOffset
            xOffset += sync.width

            // draw elements
            hueSlider.draw(mouseX)

            alphaSlider.draw(colour, mouseX)

            val hue = hueSlider.hue / 360f

            picker.draw(Color(Color.HSBtoRGB(hueSlider.hue / 360f, 1f, 1f)), mouseX, mouseY)

            val saturation = picker.saturation
            val brightness = picker.brightness

            colour = Color(Color.HSBtoRGB(hue, saturation, brightness)).integrateAlpha(255 - alphaSlider.alpha)

            rainbow.draw()
            sync.draw()

            if (factor != 1.0 && factor != 0.0) {
                RenderUtil.popScissor()
            }
        }

        setting.rainbowSaturation = picker.saturation * 100f
        setting.setValue(colour)

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            expanded.state = !expanded.state
        }

        if (expanded.getAnimationFactor() == 1.0) {
            picker.mouseClicked(mouseX, mouseY, click)
            hueSlider.mouseClicked(mouseX, mouseY, click)
            alphaSlider.mouseClicked(mouseX, mouseY, click)

            rainbow.mouseClicked(mouseX, mouseY)
            sync.mouseClicked(mouseX, mouseY)
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.getAnimationFactor() == 1.0) {
            picker.mouseReleased()
            hueSlider.mouseReleased()
            alphaSlider.mouseReleased()
        }
    }

    override fun getAbsoluteHeight(): Float {
        return height + ((picker.height + hueSlider.height + alphaSlider.height + 35) * expanded.getAnimationFactor()).toFloat()
    }

    private class Picker(var x: Float, var y: Float, var width: Float, var height: Float) {
        var dragging = false
        var saturation = 0f
        var brightness = 0f

        fun draw(colour: Color, mouseX: Float, mouseY: Float) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glDisable(GL_ALPHA_TEST)
            glShadeModel(GL_SMOOTH)

            glBegin(GL_QUADS)

            colour.glColour()
            glVertex2f(x + width, y)

            glColor4f(1f, 1f, 1f, 1f)
            glVertex2f(x, y)

            glColor4f(0f, 0f, 0f, 1f)
            glVertex2f(x, y + height)

            glColor4f(0f, 0f, 0f, 1f)
            glVertex2f(x + width, y + height)

            glEnd()

            glShadeModel(GL_FLAT)
            glEnable(GL_ALPHA_TEST)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()

            if (dragging) {
                val satDiff = min(width, max(0f, mouseX - x))

                saturation = if (satDiff == 0f) {
                    5f
                } else {
                    MathsUtil.roundDouble((satDiff / width * 100).toDouble(), 0).toFloat()
                } / 100f

                val brightDiff = min(width, max(0f, (y + height) - mouseY))

                brightness = if (brightDiff == 0f) {
                    5f
                } else {
                    MathsUtil.roundDouble((brightDiff / height * 100).toDouble(), 0).toFloat()
                } / 100f
            }

            saturation = saturation.coerceAtLeast(0.01f)
            brightness = brightness.coerceAtLeast(0.01f)

            val posX = x + (saturation * width) - 4f
            val posY = y + ((1 - brightness) * height) - 4f

            RenderUtil.drawRoundedRect(posX - 0.5f, posY - 0.5f, 8f, 8f, 5f, Color.WHITE)

            RenderUtil.drawRoundedRect(
                posX,
                posY,
                7f,
                7f,
                5f,
                Color.HSBtoRGB(
                    Color.RGBtoHSB(colour.red, colour.green, colour.blue, null)[0],
                    saturation,
                    brightness
                ).toColour()
            )
        }

        fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
            if (click == Click.LEFT) {
                if (mouseX in x..(x + width) && mouseY in y..(y + height)) {
                    dragging = true
                }
            }
        }

        fun mouseReleased() {
            dragging = false
        }
    }

    private class HueSlider(var x: Float, var y: Float, var width: Float, var height: Float) {
        var dragging = false
        var hue = 0f

        fun draw(mouseX: Float) {
            var step = 0

            for (colorIndex in 0..5) {
                RenderUtil.drawHorizontalGradientRect(x + step * (width / 6), y, width / 6, height, Color.HSBtoRGB(step / 6f, 1.0f, 1.0f).toColour(), Color.HSBtoRGB((step + 1f) / 6, 1.0f, 1.0f).toColour())

                step += 1
            }

            // Set values
            val diff = min(width, max(0f, mouseX - x))

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                hue = if (diff == 0f) {
                    0f
                } else {
                    var newValue = MathsUtil.roundDouble((diff / width * 360).toDouble(), 2).toFloat()
                    newValue = ((round((max(0f, min(360f, newValue)) * 1).toDouble()) / 1).toFloat())

                    MathsUtil.roundDouble(newValue.toDouble(), BigDecimal.valueOf(1).scale()).toFloat()
                }
            }

            val sliderMinX: Int = (x + (width * (hue / 360f))).toInt()
            RenderUtil.drawRect(sliderMinX - 1f, y, 1f, height, Color.WHITE)
        }

        fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
            if (click == Click.LEFT) {
                if (mouseX in x..(x + width) && mouseY in y..(y + height)) {
                    dragging = true
                }
            }
        }

        fun mouseReleased() {
            dragging = false
        }
    }

    private class AlphaSlider(var x: Float, var y: Float, var width: Float, var height: Float) {
        var dragging = false
        var alpha = 0f

        fun draw(colour: Color, mouseX: Float) {
            RenderUtil.drawHorizontalGradientRect(x, y, width, height, colour.integrateAlpha(255f), Color(0, 0, 0, 0))

            // Set values
            val diff = min(width, max(0f, mouseX - x))

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                alpha = if (diff == 0f) {
                    0f
                } else {
                    var newValue = MathsUtil.roundDouble((diff / width * 255).toDouble(), 2).toFloat()
                    newValue = ((round((max(0f, min(255f, newValue)) * 1).toDouble()) / 1).toFloat())

                    MathsUtil.roundDouble(newValue.toDouble(), BigDecimal.valueOf(1).scale()).toFloat()
                }
            }

            val sliderMinX: Int = (x + ((alpha / 255f) * width)).toInt()
            RenderUtil.drawRect(sliderMinX - 1f, y, 1f, height, Color.WHITE)
        }

        fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
            if (click == Click.LEFT) {
                if (mouseX in x..(x + width) && mouseY in y..(y + height)) {
                    dragging = true
                }
            }
        }

        fun mouseReleased() {
            dragging = false
        }
    }

    private class Button(val name: String, val block: Runnable, val enabledState: Supplier<Boolean>, var x: Float, var y: Float, var width: Float) {

        fun draw() {
            RenderUtil.drawRect((x + width / 2f) - 5f, y, 10f, 10f, Color(0, 0, 0, 100))
            RenderUtil.drawRect((x + width / 2f) - 4f, y + 1f, 8f, 8f, if (enabledState.get()) Colours.mainColour.value.integrateAlpha(255f) else Color(0, 0, 0, 0))

            RenderUtil.scaleTo(x + (width / 2), y + 15f, 0f, 0.6, 0.6, 0.6) {
                FontUtil.drawCenteredString(name, x + (width / 2), y + 13f, Color.WHITE)
            }
        }

        fun mouseClicked(mouseX: Float, mouseY: Float) {
            if (mouseX in x..x + width && mouseY in y..y + 20f) {
                block.run()
            }
        }

    }

}