package com.paragon.impl.ui.configuration.discord.settings.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClientFont
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.discord.settings.DiscordSetting
import com.paragon.impl.ui.util.Click
import com.paragon.util.string.StringUtil
import org.lwjgl.util.Rectangle
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordEnum(private val setting: Setting<Enum<*>>) : DiscordSetting(setting) {

    private val options = setting.value.javaClass.enumConstants
    private val optionRects = arrayOfNulls<Rectangle>(options.size)

    init {
        for (i in options.indices) {
            optionRects[i] = Rectangle()
        }
        bounds.height = (msgStyleHeight + FontUtil.getHeight()).toInt()
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        var currY = bounds.y + FontUtil.getHeight() + 1F
        var currX = bounds.x
        optionRects.forEachIndexed { i, rect ->
            (rect ?: return@forEachIndexed).setBounds(
                currX, currY.toInt(), FontUtil.getStringWidth(getFormattedName(options[i])).toInt(), FontUtil.getHeight().toInt()
            )
            if (rect.x + rect.width > bounds.x + bounds.width) {
                currX = bounds.x
                currY += FontUtil.getHeight() + 1F
                rect.x = bounds.x
                rect.y = currY.toInt()
            }

            currX += (rect.width + FontUtil.getStringWidth(", ")).toInt()

            FontUtil.drawStringWithShadow(
                getFormattedName(options[i]) + if (i != options.size - 1) "," else "", rect.x.toFloat(), rect.y.toFloat(), if (rect.contains(mouseX, mouseY)) Colours.mainColour.value else Color.WHITE
            )
        }

        val lastRect = optionRects[optionRects.size - 1]
        if (lastRect != null) {
            bounds.height = ((lastRect.y + lastRect.height) - bounds.y) + 2
        }
    }

    private fun getFormattedName(enum: Enum<*>): String {
        val str = StringUtil.getFormattedText(enum)
        return if (str == StringUtil.getFormattedText(setting.value)) if (ClientFont.isEnabled) str.uppercase() else "§n$str" else str
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (button != Click.LEFT.button) {
            return
        }

        optionRects.forEachIndexed { i, rect ->
            if (rect == null) {
                return@forEachIndexed
            }

            if (rect.contains(mouseX, mouseY)) {
                setting.setValue(options[i])
                setting.index = i
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                return
            }
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onKey(keyCode: Int) {}

}