package com.paragon.impl.ui.configuration.camper.impl.setting

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.camper.impl.ModuleElement
import com.paragon.impl.ui.configuration.camper.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

class BindElement(parent: ModuleElement, setting: Setting<Bind>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Bind>(parent, setting, x, y, width, height) {

    val listening = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x, y, width, height, hover.getColour())

        RenderUtil.scaleTo(x + 3, y + 5.5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 3f, Color.WHITE)
        }

        run {
            glScalef(0.7f, 0.7f, 0.7f)

            val factor = 1 / 0.7f

            val text = if (listening.state) "..." else setting.value.getButtonName()

            val valueX = (x + getRenderableWidth() - FontUtil.getStringWidth(text) * 0.7f - 5) * factor

            FontUtil.drawStringWithShadow(text, valueX, (y + 3.5f) * factor, Color.GRAY)

            glScalef(factor, factor, factor)
        }

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
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

            return
        }

        elements.forEach {
            it.mouseClicked(mouseX, mouseY, click)
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening.state && keyCode > 0) {
            when (keyCode) {
                Keyboard.KEY_ESCAPE, Keyboard.KEY_RETURN -> listening.state = false

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