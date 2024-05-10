package com.paragon.impl.ui.configuration.discord.settings.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.ui.configuration.discord.settings.DiscordSetting
import com.paragon.impl.ui.util.Click
import com.paragon.impl.setting.Bind
import org.lwjgl.input.Keyboard
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordBind(private val setting: Setting<Bind>) : DiscordSetting(setting) {

    private var listening = false

    init {
        bounds.height = (FontUtil.getHeight() + msgStyleHeight).toInt() + 2
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        FontUtil.drawStringWithShadow(
            if (listening) "..." else setting.value.getButtonName(), bounds.x.toFloat(), bounds.y + FontUtil.getHeight() + 1F, Color.WHITE
        )

        Paragon.INSTANCE.configurationGUI.closeOnEscape = !listening
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!bounds.contains(mouseX, mouseY)) {
            return
        }

        if (button == Click.LEFT.button) {
            listening = !listening
        }
        else if (listening) {
            setting.setValue(Bind(button, Bind.Device.MOUSE))
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            listening = false
        }
    }

    override fun onKey(keyCode: Int) {
        if (!listening) {
            return
        }

        listening = when (keyCode) {
            Keyboard.KEY_ESCAPE -> false
            Keyboard.KEY_RETURN -> false

            Keyboard.KEY_DELETE, Keyboard.KEY_BACK -> {
                setting.setValue(Bind(0, Bind.Device.KEYBOARD))
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                false
            }

            else -> {
                setting.setValue(Bind(keyCode, Bind.Device.KEYBOARD))
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                false
            }
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}

}