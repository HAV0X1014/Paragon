package com.paragon.impl.ui.configuration.phobos.element.setting.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.phobos.element.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * @author Surge
 * @since 31/07/2022
 */
class SliderElement(setting: Setting<Number>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Number>(setting, x, y, width, height) {

    private var dragging = false

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        val hovered = isHovered(mouseX, mouseY)

        var renderWidth = 0f

        /* if (setting.value is Float) {
            // Set values
            val diff = min(width, max(0f, mouseX - x))

            val min = setting.min.toFloat()
            val max = setting.max.toFloat()

            renderWidth = (width * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                } else {
                    var newValue = MathsUtil.roundDouble((diff / width * (max - min) + min).toDouble(), 2).toFloat()
                    val precision = 1 / setting.incrementation.toFloat()
                    newValue = round(max(min, min(max, newValue)) * precision) / precision
                    setting.setValue(newValue)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        } else if (setting.value is Double) {
            // Set values
            val diff = min(width, max(0f, mouseX - x)).toDouble()

            val min = setting.min.toDouble()
            val max = setting.max.toDouble()

            renderWidth = (width * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                } else {
                    var newValue = MathsUtil.roundDouble(diff / width * (max - min) + min, 2)
                    val precision = (1 / setting.incrementation.toFloat()).toDouble()

                    newValue = round(max(min, min(max, newValue)) * precision) / precision

                    setting.setValue(newValue)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        } */

        if (setting.value is Float) {
            // Set values
            val diff = min(width, max(0f, mouseX - (x + 4)))

            val min = setting.min.toFloat()
            val max = setting.max.toFloat()

            renderWidth = (width * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                } else {
                    var newValue = MathsUtil.roundDouble((diff / width * (max - min) + min).toDouble(), 2).toFloat()
                    val precision = 1 / setting.incrementation.toFloat()

                    newValue = (round(max(min, min(max, newValue)) * precision) / precision)

                    setting.setValue(
                        MathsUtil.roundDouble(
                            newValue.toDouble(), BigDecimal.valueOf(setting.incrementation.toDouble()).scale()
                        ).toFloat()
                    )
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        } else if (setting.value is Double) {
            // Set values
            val diff = min(width, max(0f, mouseX - (x + 4))).toDouble()

            val min = setting.min.toDouble()
            val max = setting.max.toDouble()

            renderWidth = (width * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                } else {
                    var newValue = MathsUtil.roundDouble(diff / width * (max - min) + min, 2)
                    val precision = (1 / setting.incrementation.toFloat()).toDouble()

                    newValue = round(max(min, min(max, newValue)) * precision) / precision

                    setting.setValue(
                        MathsUtil.roundDouble(newValue, BigDecimal.valueOf(setting.incrementation.toDouble()).scale())
                    )
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        }

        if (isHovered(mouseX, mouseY)) {
            RenderUtil.drawRect(x + renderWidth, y, width - renderWidth, height, Color(200, 200, 200, 150))
        }

        RenderUtil.drawRect(
            x,
            y,
            renderWidth,
            height,
            Colours.mainColour.value.integrateAlpha(if (hovered) 205f else 150f)
        )

        glScalef(0.85f, 0.85f, 0.85f).let {
            val factor = 1 / 0.85f

            FontUtil.drawStringWithShadow(setting.name, (x + 4) * factor, (y + 4) * factor, Color.WHITE)

            val valueX = (x + width - FontUtil.getStringWidth(setting.value.toString()) * 0.85f - 3) * factor
            FontUtil.drawStringWithShadow(setting.value.toString(), valueX, (y + 4f) * factor, Color(190, 190, 190))

            glScalef(factor, factor, factor)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.LEFT) {
            dragging = true
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        dragging = false
    }

}