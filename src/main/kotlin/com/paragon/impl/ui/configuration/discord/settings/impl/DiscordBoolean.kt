package com.paragon.impl.ui.configuration.discord.settings.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil

import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.discord.settings.DiscordSetting
import com.paragon.impl.ui.util.Click
import org.lwjgl.util.Rectangle
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordBoolean(private val setting: Setting<Boolean>) : DiscordSetting(setting) {

    private val stateRect = Rectangle()

    init {
        bounds.height = (FontUtil.getHeight() + msgStyleHeight).toInt() + 2
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        stateRect.setBounds(
            bounds.x, (bounds.y + FontUtil.getHeight() + 1).toInt(), FontUtil.getStringWidth(setting.value.toString()).toInt(), FontUtil.getHeight().toInt()
        )

        FontUtil.drawStringWithShadow(
            setting.value.toString(), bounds.x.toFloat(), bounds.y + FontUtil.getHeight() + 1F, if (stateRect.contains(mouseX, mouseY)) Colours.mainColour.value else Color.WHITE
        )
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (button != Click.LEFT.button) {
            return
        }
        if (stateRect.contains(mouseX, mouseY)) {
            setting.setValue(!setting.value)
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onKey(keyCode: Int) {}

}