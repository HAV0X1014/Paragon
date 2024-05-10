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
import com.paragon.impl.setting.Bind
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 */
class BindElement(parent: ModuleElement, setting: Setting<Bind>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Bind>(parent, setting, x, y, width, height) {

    private val listening = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x + 3, y + 3, width - 4, height - 4, Color(100, 100, 100))
        RenderUtil.drawRect(x + 2, y + 2, width - 4, height - 4, Color(130, 130, 130))

        RenderUtil.drawHorizontalGradientRect(x + 2, y + 2, ((width - 4) * listening.getAnimationFactor()).toFloat(), height - 4, Colours.mainColour.value, if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter() else Colours.mainColour.value)

        glScalef(0.8f, 0.8f, 0.8f)

        val scaleFactor = 1 / 0.8f
        FontUtil.drawStringWithShadow(setting.name, (x + 5) * scaleFactor, (y + 5f) * scaleFactor, Color.WHITE)

        val value = if (listening.state) "..." else setting.value.getButtonName()

        val valueX: Float = (x + width - FontUtil.getStringWidth(value) * 0.8f - 5) * scaleFactor

        FontUtil.drawStringWithShadow(value, valueX, (y + 5f) * scaleFactor, Color(190, 190, 190))

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        val scissorY = MathHelper.clamp(y, parent.parent.y + parent.parent.height, (parent.parent.y + parent.parent.height + parent.parent.scissorHeight) - getTotalHeight())

        if (expanded.getAnimationFactor() > 0) {
            var yOffset = 0f

            RenderUtil.pushScissor(x, scissorY, width, getTotalHeight())

            subSettings.forEach {
                it.x = x + 2
                it.y = y + height + yOffset

                it.draw(mouseX, mouseY, mouseDelta)

                yOffset += it.getTotalHeight()
            }

            RenderUtil.popScissor()
        }

        Paragon.INSTANCE.configurationGUI.closeOnEscape = !listening.state
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (y in parent.parent.y + parent.parent.height..parent.parent.y + parent.parent.height + parent.parent.scissorHeight) {
            if (click == Click.LEFT && isHovered(mouseX, mouseY)) {
                listening.state = !listening.state
            }

            else if (click == Click.RIGHT && isHovered(mouseX, mouseY)) {
                expanded.state = !expanded.state
            }

            else if (listening.state) {
                setting.setValue(Bind(click.button, Bind.Device.MOUSE))
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                listening.state = false
            }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening.state && keyCode > 0) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE -> listening.state = false
                Keyboard.KEY_RETURN -> listening.state = false

                Keyboard.KEY_DELETE, Keyboard.KEY_BACK -> {
                    setting.setValue(Bind(0, Bind.Device.KEYBOARD))
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))

                    listening.state = false
                }

                else -> {
                    setting.setValue(Bind(keyCode, Bind.Device.KEYBOARD))
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))

                    listening.state = false
                }
            }
        }
    }

}