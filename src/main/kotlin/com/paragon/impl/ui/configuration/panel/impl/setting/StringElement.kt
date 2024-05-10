package com.paragon.impl.ui.configuration.panel.impl.setting

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.panel.impl.ModuleElement
import com.paragon.impl.ui.configuration.panel.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.*
import java.awt.Color

class StringElement(parent: ModuleElement, setting: Setting<String>, x: Float, y: Float, width: Float, height: Float) : SettingElement<String>(parent, setting, x, y, width, height) {

    val listening = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x, y, width, height, hover.getColour())

        RenderUtil.scaleTo(x + 3, y + 5.5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 5.5f, Color.WHITE)
        }

        RenderUtil.drawRoundedRect(
            x + getRenderableWidth() - FontUtil.getStringWidth(setting.value) * 0.7f - 8,
            y + 2f,
            FontUtil.getStringWidth(setting.value) * 0.7f + 6,
            height - 4f,
            5f,
            Color(50 + (10 * listening.getAnimationFactor()).toInt(), 50 + (10 * listening.getAnimationFactor()).toInt(), 50 + (10 * listening.getAnimationFactor()).toInt())
        )

        run {
            glScalef(0.7f, 0.7f, 0.7f)

            val scaleFactor = 1 / 0.7f

            val valueX: Float = (x + getRenderableWidth() - FontUtil.getStringWidth(setting.value) * 0.7f - 5) * scaleFactor

            FontUtil.drawStringWithShadow(setting.value, valueX, (y + 5f) * scaleFactor, Color.GRAY)

            glScalef(scaleFactor, scaleFactor, scaleFactor)
        }

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            if (click == Click.LEFT) {
                listening.state = !listening.state
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }

            return
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening.state) {
            if (keyCode == Keyboard.KEY_BACK) {
                if (setting.value.isNotEmpty()) {
                    setting.setValue(setting.value.substring(0, setting.value.length - 1))
                    Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                }
            }
            else if (keyCode == Keyboard.KEY_RETURN) {
                listening.state = false
            }
            else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                setting.setValue(setting.value + character)
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
        }
    }

}