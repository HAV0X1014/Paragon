package com.paragon.impl.ui.configuration.camper.impl.setting

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.camper.impl.ModuleElement
import com.paragon.impl.ui.configuration.camper.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class SliderElement(parent: ModuleElement, setting: Setting<Number>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Number>(parent, setting, x, y, width, height) {

    private val sliderAnimation = Animation({ 800f }, false, { Easing.LINEAR })
    private var sliderWidth = 0f
    var dragging = false

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        val maxWidth = width

        var localWidth = 0f

        if (setting.value is Float) {
            // Set values
            val diff = min(maxWidth, max(0f, mouseX - x))

            val min = setting.min.toFloat()
            val max = setting.max.toFloat()

            localWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                } else {
                    var newValue = MathsUtil.roundDouble((diff / maxWidth * (max - min) + min).toDouble(), 2).toFloat()
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
            val diff = min(maxWidth, max(0f, mouseX - x)).toDouble()

            val min = setting.min.toDouble()
            val max = setting.max.toDouble()

            localWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                } else {
                    var newValue = MathsUtil.roundDouble(diff / maxWidth * (max - min) + min, 2)
                    val precision = (1 / setting.incrementation.toFloat()).toDouble()

                    newValue = round(max(min, min(max, newValue)) * precision) / precision

                    setting.setValue(
                        MathsUtil.roundDouble(newValue, BigDecimal.valueOf(setting.incrementation.toDouble()).scale())
                    )
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
        }

        sliderAnimation.state = sliderWidth != localWidth

        if (localWidth > sliderWidth) {
            val widthDifference: Float = sliderWidth - localWidth
            sliderWidth -= widthDifference * sliderAnimation.getAnimationFactor().toFloat()
        }

        if (localWidth < sliderWidth) {
            val widthDifference: Float = localWidth - sliderWidth
            sliderWidth += widthDifference * sliderAnimation.getAnimationFactor().toFloat()
        }

        sliderWidth = sliderWidth.coerceIn(0f, maxWidth)

        RenderUtil.drawRect(x, y, width, height, Color(0, 0, 0, 100))
        RenderUtil.drawRect(x, y, sliderWidth, height, Colours.mainColour.value.fade(Colours.mainColour.value.darker(), hover.getAnimationFactor()))

        RenderUtil.scaleTo(x + 3, y + 5.5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 3f, Color.WHITE)
        }

        run {
            glScalef(0.7f, 0.7f, 0.7f)

            val factor = 1 / 0.7f

            val valueX = (x + getRenderableWidth() - FontUtil.getStringWidth(setting.value.toString()) * 0.7f - 5) * factor

            FontUtil.drawStringWithShadow(setting.value.toString(), valueX, (y + 3.5f) * factor, Color.GRAY)

            glScalef(factor, factor, factor)
        }

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            if (click == Click.LEFT) {
                dragging = true
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }

            return
        }

        elements.forEach {
            it.mouseClicked(mouseX, mouseY, click)
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        dragging = false
    }

}