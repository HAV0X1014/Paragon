package com.paragon.impl.ui.configuration.retrowindows.element.setting

import com.paragon.impl.setting.Setting
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.impl.ui.configuration.retrowindows.element.setting.elements.*
import com.paragon.impl.ui.configuration.shared.RawElement
import com.paragon.impl.ui.util.Click
import com.paragon.impl.setting.Bind
import me.surge.animation.Animation
import java.awt.Color

/**
 * @author Surge
 */
abstract class SettingElement<T>(val parent: ModuleElement, val setting: Setting<T>, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    val expanded = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)
    val subSettings = ArrayList<SettingElement<*>>()

    init {
        setting.subsettings.forEach {
            when (it.value) {
                is Boolean -> subSettings.add(BooleanElement(parent, it as Setting<Boolean>, x + 2, y, width - 4, height))
                is Enum<*> -> subSettings.add(EnumElement(parent, it as Setting<Enum<*>>, x + 2, y, width - 4, height))
                is Number -> subSettings.add(SliderElement(parent, it as Setting<Number>, x + 2, y, width - 4, height))
                is Bind -> subSettings.add(BindElement(parent, it as Setting<Bind>, x + 2, y, width - 4, height))
                is String -> subSettings.add(StringElement(parent, it as Setting<String>, x + 2, y, width - 4, height))
                is Color -> subSettings.add(ColourElement(parent, it as Setting<Color>, x + 2, y, width - 4, height))
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (isHovered(mouseX, mouseY) && y > parent.parent.y + parent.parent.height) {
            parent.parent.tooltipName = setting.name
            parent.parent.tooltipContent = setting.description
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (expanded.state) {
            subSettings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.state) {
            subSettings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded.state) {
            subSettings.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    open fun getSubSettingHeight(): Float {
        var height = 0f

        subSettings.forEach {
            if (it.setting.isVisible()) {
                height += it.getTotalHeight()
            }
        }

        return height
    }

    fun getTotalHeight() = (height + (getSubSettingHeight() * expanded.getAnimationFactor())).toFloat()

}