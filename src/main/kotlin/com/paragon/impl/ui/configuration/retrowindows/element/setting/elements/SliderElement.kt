package com.paragon.impl.ui.configuration.retrowindows.element.setting.elements

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.impl.ui.configuration.retrowindows.element.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.render.RenderUtil
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

/**
 * @author Surge
 */
class SliderElement(parent: ModuleElement, setting: Setting<Number>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Number>(parent, setting, x, y, width, height) {

    private var renderWidth = 0f
    var dragging = false

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x + 3, y + 3, width - 4, height - 4, Color(100, 100, 100))
        RenderUtil.drawRect(x + 2, y + 2, width - 4, height - 4, Color(130, 130, 130))

        val maxWidth = width - 4

        if (setting.value is Float) {
            // Set values
            val diff = min(maxWidth, max(0f, mouseX - (x + 2)))

            val min = setting.min.toFloat()
            val max = setting.max.toFloat()

            val rWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (renderWidth > rWidth) {
                renderWidth--
            }

            if (renderWidth < rWidth) {
                renderWidth++
            }

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0f) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
                else {
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
        }
        else if (setting.value is Double) {
            // Set values
            val diff = min(maxWidth, max(0f, mouseX - (x + 2))).toDouble()

            val min = setting.min.toDouble()
            val max = setting.max.toDouble()

            val rWidth = (maxWidth * (setting.value.toDouble() - min) / (max - min)).toFloat()

            if (renderWidth > rWidth) {
                val difference = renderWidth - rWidth
                renderWidth -= difference / 2
            }

            if (renderWidth < rWidth) {
                val difference = rWidth - renderWidth
                renderWidth += difference / 2
            }

            if (!Mouse.isButtonDown(0)) {
                dragging = false
            }

            if (dragging) {
                if (diff == 0.0) {
                    setting.setValue(setting.min)
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
                else {
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

        RenderUtil.drawHorizontalGradientRect(
            x + 2, y + 2, renderWidth, height - 4, Colours.mainColour.value, if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter() else Colours.mainColour.value
        )

        glScalef(0.8f, 0.8f, 0.8f)

        val scaleFactor = 1 / 0.8f
        FontUtil.drawStringWithShadow(setting.name, (x + 5) * scaleFactor, (y + 5f) * scaleFactor, Color.WHITE)

        val valueX: Float = (x + width - FontUtil.getStringWidth(setting.value.toString()) * 0.8f - 5) * scaleFactor

        FontUtil.drawStringWithShadow(
            setting.value.toString(), valueX, (y + 5f) * scaleFactor, Color(190, 190, 190)
        )

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        if (expanded.getAnimationFactor() > 0) {
            var yOffset = 0f

            val scissorY = MathHelper.clamp(
                y + height, parent.parent.y + parent.parent.height, ((parent.parent.y + parent.parent.scissorHeight) - getSubSettingHeight() * expanded.getAnimationFactor().toFloat()) + height
            ).toDouble()

            val scissorHeight = MathHelper.clamp(
                getSubSettingHeight().toDouble() * expanded.getAnimationFactor(), 0.0, parent.parent.scissorHeight.toDouble()
            )

            RenderUtil.pushScissor(x, scissorY.toFloat(), width, scissorHeight.toFloat())

            subSettings.forEach {
                it.x = x + 2
                it.y = y + height + yOffset

                it.draw(mouseX, mouseY, mouseDelta)

                yOffset += it.getTotalHeight()
            }

            RenderUtil.popScissor()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && y in parent.parent.y + parent.parent.height..parent.parent.y + parent.parent.height + parent.parent.scissorHeight) {
            if (click == Click.LEFT) {
                dragging = true
            }
            else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        dragging = false
    }

}