package com.paragon.impl.ui.configuration.camper.impl

import com.paragon.impl.module.Module
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.camper.CamperCheatGUI
import com.paragon.impl.ui.configuration.camper.impl.setting.*
import com.paragon.impl.ui.configuration.shared.RawElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import net.minecraft.util.math.MathHelper
import java.awt.Color

class ModuleElement(val parent: CategoryPanel, val module: Module, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    private val hover = ColourAnimation(Color(0, 0, 0, 100), Color(0, 0, 0, 150), 100f, false, Easing.LINEAR)
    private val enabled = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })
    private val expanded = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    val elements = arrayListOf<SettingElement<*>>()

    init {
        module.settings.forEach {
            when (it.value) {
                is Boolean -> {
                    elements.add(BooleanElement(this, it as Setting<Boolean>, x + 1, y, width - 1, 12f))
                }

                is Enum<*> -> {
                    elements.add(EnumElement(this, it as Setting<Enum<*>>, x + 1, y, width - 1, 12f))
                }

                is Number -> {
                    elements.add(SliderElement(this, it as Setting<Number>, x + 1, y, width - 1, 12f))
                }

                is Bind -> {
                    elements.add(BindElement(this, it as Setting<Bind>, x + 1, y, width - 1, 12f))
                }

                is Color -> {
                    elements.add(ColourElement(this, it as Setting<Color>, x + 1, y, width - 1, 12f))
                }

                is String -> {
                    elements.add(StringElement(this, it as Setting<String>, x + 1, y, width - 1, 12f))
                }
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        if (isHovered(mouseX, mouseY)) {
        	CamperCheatGUI.description = module.description
        }

        hover.state = isHovered(mouseX, mouseY)
        enabled.state = module.isEnabled

        RenderUtil.drawRect(x, y, width, height, (hover.getColour().fade(Colours.mainColour.value.fade(Colours.mainColour.value.darker(), hover.getAnimationFactor()), enabled.getAnimationFactor())))

        RenderUtil.scaleTo(x + 3, y + 5f, 0f, 0.9, 0.9, 0.9) {
            FontUtil.drawStringWithShadow(module.name, x + 3, y + 2.5f, Color.WHITE)
        }

        if (expanded.getAnimationFactor() > 0) {
            var offset = y + height + 1
            val factor = expanded.getAnimationFactor()

            if (factor != 1.0 && factor != 0.0) {
                RenderUtil.pushScissor(x,
                    MathHelper.clamp(y + 4f, parent.y + parent.height, 100000f),
                    width,
                    MathHelper.clamp(getAbsoluteHeight() - 4, 0f, (parent.y + parent.height + parent.moduleHeight).toFloat() - (y + 4f))
                )
            }

            elements.forEach {
                if (it.setting.isVisible()) {
                    it.x = x + 1
                    it.y = offset

                    it.draw(mouseX, mouseY, mouseDelta)

                    offset += it.getAbsoluteHeight() + 1
                }
            }

            RenderUtil.drawRect(x, y + height, 1f, getAbsoluteHeight() - height - 1, Colours.mainColour.value.fade(Colours.mainColour.value.darker(), hover.getAnimationFactor()))

            if (factor != 1.0 && factor != 0.0) {
                RenderUtil.popScissor()
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
        return height + ((elements.filter { it.setting.isVisible() }.sumOf { it.getAbsoluteHeight().toDouble() + 1 } + 1) * expanded.getAnimationFactor()).toFloat()
    }

}