package com.paragon.impl.ui.configuration.retrowindows.element.module

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.retrowindows.element.CategoryWindow
import com.paragon.impl.ui.configuration.retrowindows.element.setting.SettingElement
import com.paragon.impl.ui.configuration.retrowindows.element.setting.elements.*
import com.paragon.impl.ui.configuration.shared.RawElement
import com.paragon.impl.ui.util.Click
import com.paragon.impl.setting.Bind
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 */
class ModuleElement(val parent: CategoryWindow, val module: Module, x: Float, y: Float, width: Float, height: Float) : RawElement(x, y, width, height) {

    private val expanded = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)
    val enabled = Animation(ClickGUI.animationSpeed::value, module.isEnabled, ClickGUI.easing::value)
    private val hover = Animation({ 100f }, false, ClickGUI.easing::value)

    val settings = ArrayList<SettingElement<*>>()

    init {
        module.settings.forEach {
            when (it.value) {
                is Boolean -> settings.add(BooleanElement(this, it as Setting<Boolean>, x + 2, y, width - 4, height))
                is Enum<*> -> settings.add(EnumElement(this, it as Setting<Enum<*>>, x + 2, y, width - 4, height))
                is Number -> settings.add(SliderElement(this, it as Setting<Number>, x + 2, y, width - 4, height))
                is Bind -> settings.add(BindElement(this, it as Setting<Bind>, x + 2, y, width - 4, height))
                is String -> settings.add(StringElement(this, it as Setting<String>, x + 2, y, width - 4, height))
                is Color -> settings.add(ColourElement(this, it as Setting<Color>, x + 2, y, width - 4, height))
            }
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        hover.state = isHovered(mouseX, mouseY)
        enabled.state = module.isEnabled

        if (hover.state && y > parent.y + parent.height && y < parent.y + parent.height + parent.scissorHeight) {
            parent.tooltipName = module.name
            parent.tooltipContent = module.description
        }

        RenderUtil.drawRect(x + 3, y + 3, width - 4, getTotalHeight() - 4, Color(100, 100, 100))
        RenderUtil.drawRect(x + 2, y + 2, width - 4, getTotalHeight() - 4, Color(120 - (10 * hover.getAnimationFactor()).toInt(), 120 - (10 * hover.getAnimationFactor()).toInt(), 120 - (10 * hover.getAnimationFactor()).toInt()))

        RenderUtil.drawHorizontalGradientRect(x + 2, y + 2, ((width - 4) * enabled.getAnimationFactor()).toFloat(), height - 4, Colours.mainColour.value, if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter() else Colours.mainColour.value)

        glScalef(0.9f, 0.9f, 0.9f)

        val scaleFactor = 1 / 0.9f
        FontUtil.drawStringWithShadow(module.name, (x + 5) * scaleFactor, (y + 4.5f) * scaleFactor, Color.WHITE)

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        if (expanded.getAnimationFactor() > 0) {
            var yOffset = -2f

            val scissorY = MathHelper.clamp(y, parent.y + parent.height, (parent.y + parent.height + parent.scissorHeight) - getTotalHeight())

            RenderUtil.pushScissor(x, scissorY, width, getTotalHeight())

            settings.forEach {
                if (it.setting.isVisible()) {
                    it.x = x + 2
                    it.y = y + height + yOffset

                    if (it.y + it.height < parent.y + parent.height + parent.scissorHeight) {
                        it.draw(mouseX, mouseY, mouseDelta)
                    }

                    yOffset += it.getTotalHeight()
                }
            }

            RenderUtil.popScissor()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && y in parent.y + parent.height..parent.y + parent.height + parent.scissorHeight) {
            if (click == Click.LEFT) {
                module.toggle()
            }
            else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }

        if (expanded.state) {
            settings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseClicked(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded.state) {
            settings.forEach {
                if (it.setting.isVisible()) {
                    it.mouseReleased(mouseX, mouseY, click)
                }
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded.state) {
            settings.forEach {
                if (it.setting.isVisible()) {
                    it.keyTyped(character, keyCode)
                }
            }
        }
    }

    private fun getSettingHeight(): Float {
        // offset
        var height = 0f

        settings.forEach {
            if (it.setting.isVisible()) {
                height += it.getTotalHeight()
            }
        }

        return height
    }

    fun getTotalHeight() = (height + (getSettingHeight() * expanded.getAnimationFactor())).toFloat()

}