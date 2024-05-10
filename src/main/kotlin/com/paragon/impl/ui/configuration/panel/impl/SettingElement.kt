package com.paragon.impl.ui.configuration.panel.impl

import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.panel.PanelGUI
import com.paragon.impl.ui.configuration.panel.impl.setting.*
import com.paragon.impl.ui.configuration.shared.RawElement
import com.paragon.impl.ui.configuration.panel.impl.SettingElement
import com.paragon.impl.ui.configuration.panel.impl.setting.StringElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import java.awt.Color

open class SettingElement<T>(val parent: ModuleElement, val setting: Setting<T>, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    val hover = ColourAnimation(Color(25, 25, 25), Color(35, 35, 35), { 100f }, false, Easing.LINEAR)
    val expanded = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    val elements = arrayListOf<SettingElement<*>>()

    init {
        setting.subsettings.forEach {
            when (it.value) {
                is Boolean -> {
                    elements.add(BooleanElement(parent, it as Setting<Boolean>, x + 1, y, width - 1, 16f))
                }

                is Enum<*> -> {
                    elements.add(EnumElement(parent, it as Setting<Enum<*>>, x + 1, y, width - 1, 16f))
                }

                is Number -> {
                    elements.add(SliderElement(parent, it as Setting<Number>, x + 1, y, width - 1, 24f))
                }

                is Bind -> {
                    elements.add(BindElement(parent, it as Setting<Bind>, x + 1, y, width - 1, 16f))
                }

                is Color -> {
                    elements.add(ColourElement(parent, it as Setting<Color>, x, y, width - 1, 16f))
                }

                is String -> {
                    elements.add(StringElement(parent, it as Setting<String>, x, y, width - 1, 16f))
                }
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (isHovered(mouseX, mouseY)) {
            PanelGUI.description = setting.description
        }
        hover.state = isHovered(mouseX, mouseY)
    }

    fun drawSettings(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (expanded.getAnimationFactor() > 0) {
            var offset = y + height

            elements.forEach {
                if (it.setting.isVisible()) {
                    it.x = x + 1
                    it.y = offset

                    it.draw(mouseX, mouseY, mouseDelta)

                    offset += it.getAbsoluteHeight()
                }
            }

            RenderUtil.drawRect(x, y + height, 1f, offset - y - height, Colours.mainColour.value)
        }

        if (elements.any { it.setting.isVisible() }) {
            RenderUtil.rotate((90 * expanded.getAnimationFactor()).toFloat(), x + width - 9f, y + 8.5f, 0f) {
                RenderUtil.drawTriangle(x + width - 9, y + 8.5f, 6f, 8f, hover.getColour().brighter())
            }
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        if (expanded.getAnimationFactor() > 0) {
            elements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        if (expanded.getAnimationFactor() == 1.0) {
            elements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        if (expanded.getAnimationFactor() == 1.0) {
            elements.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    fun getRenderableWidth(): Float {
        return if (setting.subsettings.any { it.isVisible() }) parent.parent.width - 15f else parent.parent.width
    }

    open fun getAbsoluteHeight(): Float {
        return height + (elements.filter { it.setting.isVisible() }.sumOf { it.getAbsoluteHeight().toDouble() } * expanded.getAnimationFactor()).toFloat()
    }

}