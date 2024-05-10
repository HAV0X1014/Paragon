package com.paragon.impl.ui.configuration.phobos.element.setting

import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.phobos.element.setting.impl.*
import com.paragon.impl.ui.configuration.shared.RawElement
import com.paragon.impl.ui.util.Click
import com.paragon.phobosgui.simple.element.setting.impl.ColourElement
import java.awt.Color
import java.util.stream.Collectors

/**
 * @author Surge
 * @since 31/07/2022
 */
abstract class SettingElement<T>(val setting: Setting<T>, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    private var expanded = false
    val settings: ArrayList<SettingElement<*>> = ArrayList()

    init {
        setting.subsettings.forEach {
            when (it.value) {
                is Boolean -> {
                    settings.add(BooleanElement(it as Setting<Boolean>, x + 2, y, width - 4, height))
                }

                is Enum<*> -> {
                    settings.add(EnumElement(it as Setting<Enum<*>>, x + 2, y, width - 4, height))
                }

                is Number -> {
                    settings.add(SliderElement(it as Setting<Number>, x + 2, y, width - 4, height))
                }

                is Color -> {
                    settings.add(ColourElement(it as Setting<Color>, x + 2, y, width - 4, height))
                }

                is Bind -> {
                    settings.add(BindElement(it as Setting<Bind>, x + 2, y, width - 4, height))
                }

                is String -> {
                    settings.add(StringElement(it as Setting<String>, x + 2, y, width - 4, height))
                }
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (expanded) {
            var y = y + height + 1.5f

            settings.forEach {
                if (it.setting.isVisible()) {
                    it.x = x + 2
                    it.y = y

                    it.draw(mouseX, mouseY, mouseDelta)

                    y += it.getAbsoluteHeight() + 0.5f
                }
            }
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.RIGHT && settings.stream().filter { it.setting.isVisible() }.collect(Collectors.toList()).size > 0) {
            expanded = !expanded
        }

        if (expanded) {
            settings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded) {
            settings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded) {
            settings.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    fun getSettingHeight(): Float {
        var height = 1.5f

        settings.forEach {
            if (it.setting.isVisible()) {
                height += it.getAbsoluteHeight() + 0.5f
            }
        }

        return height
    }

    fun getAbsoluteHeight(): Float {
        return height + if (expanded) getSettingHeight() else 0f
    }

}