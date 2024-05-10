package com.paragon.impl.ui.configuration.panel.impl

import com.paragon.impl.module.Module
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.panel.PanelGUI
import com.paragon.impl.ui.configuration.panel.impl.setting.*
import com.paragon.impl.ui.configuration.shared.RawElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import java.awt.Color

class ModuleElement(val parent: CategoryPanel, val module: Module, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    private val hover = ColourAnimation(Color(40, 40, 40), Color(50, 50, 50), 100f, false, Easing.LINEAR)
    private val enabled = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })
    private val expanded = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    val elements = arrayListOf<SettingElement<*>>()

    init {
        module.settings.forEach {
            when (it.value) {
                is Boolean -> {
                    elements.add(BooleanElement(this, it as Setting<Boolean>, x, y, width, 16f))
                }

                is Enum<*> -> {
                    elements.add(EnumElement(this, it as Setting<Enum<*>>, x , y, width, 16f))
                }

                is Number -> {
                    elements.add(SliderElement(this, it as Setting<Number>, x, y, width, 24f))
                }

                is Bind -> {
                    elements.add(BindElement(this, it as Setting<Bind>, x, y, width, 16f))
                }

                is Color -> {
                    elements.add(ColourElement(this, it as Setting<Color>, x, y, width, 16f))
                }

                is String -> {
                    elements.add(StringElement(this, it as Setting<String>, x, y, width, 16f))
                }
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (isHovered(mouseX, mouseY)) {
        	PanelGUI.description = module.description
        }

        hover.state = isHovered(mouseX, mouseY)
        enabled.state = module.isEnabled

        RenderUtil.drawRect(x, y, parent.width, height, hover.getColour())

        RenderUtil.scaleTo(x + 3, y + 5f, 0f, 0.9, 0.9, 0.9) {
            FontUtil.drawStringWithShadow(
                module.name,
                x + 3,
                y + 5f,
                Color(180, 180, 180).fade(Colours.mainColour.value, enabled.getAnimationFactor())
            )
        }

        if (module.settings.size > 2) {
            RenderUtil.rotate((90 * expanded.getAnimationFactor()).toFloat(), x + parent.width - 9f, y + 8.5f, 0f) {
                RenderUtil.drawTriangle(x + parent.width - 9, y + 8.5f, 6f, 8f, hover.getColour().brighter())
            }
        }

        if (expanded.getAnimationFactor() > 0) {
            var offset = y + height

            elements.forEach {
                if (it.setting.isVisible()) {
                    it.x = x
                    it.y = offset

                    it.draw(mouseX, mouseY, mouseDelta)

                    offset += it.getAbsoluteHeight()
                }
            }
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY)) {
            if (click == Click.LEFT) {
                module.toggle()
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }

            return
        }

        if (expanded.getAnimationFactor() > 0) {
            elements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.getAnimationFactor() > 0) {
            elements.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded.getAnimationFactor() > 0) {
            elements.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    fun getAbsoluteHeight(): Float {
        return height + (elements.filter { it.setting.isVisible() }.sumOf { it.getAbsoluteHeight().toDouble() } * expanded.getAnimationFactor()).toFloat()
    }

}