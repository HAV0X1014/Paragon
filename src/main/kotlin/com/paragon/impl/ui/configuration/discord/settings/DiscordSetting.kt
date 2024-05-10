package com.paragon.impl.ui.configuration.discord.settings

import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.discord.IRenderable
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.scaleTo
import org.lwjgl.util.Rectangle
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author SooStrator1136
 */
abstract class DiscordSetting(val dSetting: Setting<*>) : IRenderable {

    val bounds = Rectangle()
    val msgStyleHeight = FontUtil.getHeight() + 1F

    override fun render(mouseX: Int, mouseY: Int) {
        if (bounds.contains(mouseX, mouseY)) {
            RenderUtil.drawRect(
                bounds.x.toFloat(), bounds.y.toFloat(), bounds.width.toFloat(), bounds.height.toFloat(), DiscordGUI.msgHovered
            )
        }

        FontUtil.drawStringWithShadow(
            dSetting.name, bounds.x.toFloat(), bounds.y.toFloat(), Colours.mainColour.value
        )

        //Render time in 12h format
        run {
            val dateX = bounds.x + FontUtil.getStringWidth(dSetting.name) + 2F
            val dateY = bounds.y + (FontUtil.getHeight() / 2F)
            val scaleFac = (FontUtil.getHeight() / 2.0) / FontUtil.getHeight()
            scaleTo(dateX, dateY, 0F, scaleFac, scaleFac, 1.0) {
                FontUtil.drawStringWithShadow(
                    SimpleDateFormat("hh:mm a").format(Date(System.currentTimeMillis())), dateX, dateY, Colours.mainColour.value
                )
            }
        }
    }

}