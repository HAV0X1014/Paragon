package com.paragon.impl.ui.configuration.discord.settings.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.ui.configuration.discord.settings.DiscordSetting
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.Timer
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordString(private val setting: Setting<String>) : DiscordSetting(setting) {

    private val cursorTimer = Timer()
    private var typing = false

    init {
        bounds.height = (FontUtil.getHeight() + msgStyleHeight).toInt() + 2
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        FontUtil.drawStringWithShadow(
            setting.value, bounds.x.toFloat(), bounds.y + FontUtil.getHeight() + 1F, Color.WHITE
        )

        if (typing) {
            if (cursorTimer.hasMSPassed(500.0)) {
                FontUtil.drawStringWithShadow(
                    "|", bounds.x + FontUtil.getStringWidth(setting.value) + 1F, bounds.y + FontUtil.getHeight() + 1F, Color.WHITE
                )

                if (cursorTimer.hasMSPassed(1000.0)) {
                    cursorTimer.reset()
                }
            }
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!typing && bounds.contains(mouseX, mouseY) && button == Click.LEFT.button) {
            typing = true
        }
    }

    override fun onKey(keyCode: Int) {
        if (!typing) {
            return
        }

        val character = Keyboard.getEventCharacter()

        if (keyCode == Keyboard.KEY_BACK) {
            if (setting.value.isNotEmpty()) {
                setting.setValue(setting.value.substring(0, setting.value.length - 1))
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
        }
        else if (keyCode == Keyboard.KEY_RETURN) {
            typing = false
        }
        else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
            setting.setValue(setting.value + character)
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}

}