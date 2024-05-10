package com.paragon.impl.ui.configuration.discord.module

import com.paragon.impl.module.Module
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.discord.IRenderable
import com.paragon.util.render.RenderUtil
import org.lwjgl.util.Rectangle
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordModule(val module: Module) : IRenderable {

    val rect = Rectangle()

    override fun render(mouseX: Int, mouseY: Int) {
        val isHovered = ModuleBar.rect.contains(mouseX, mouseY) && rect.contains(mouseX, mouseY)
        if (isHovered || module == ModuleBar.focusedModule) {
            RenderUtil.drawRoundedRect(
                rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat(), 2f, DiscordGUI.channelHoveredColor
            )
        }

        FontUtil.drawStringWithShadow(
            "# ${module.name}", rect.x + 5F, rect.y + (rect.height / 2F) - (FontUtil.getHeight() / 2), DiscordGUI.channelTextColor
        )

        //Tooltip
        if (isHovered) {
            RenderUtil.drawRoundedRect(
                mouseX - 4f, (mouseY - (FontUtil.getHeight() / 2f)) - 1f, FontUtil.getStringWidth(module.description) + 8f, FontUtil.getHeight() + 2f, 5f, DiscordGUI.channelHoveredColor
            )
            RenderUtil.drawRoundedOutline(
                mouseX - 3f, (mouseY - (FontUtil.getHeight() / 2f)) - 2f, FontUtil.getStringWidth(module.description) + 6f, FontUtil.getHeight() + 4f, 5f, 1f, DiscordGUI.categoryBarBackground
            )

            FontUtil.drawStringWithShadow(
                module.description, mouseX.toFloat(), mouseY - (FontUtil.getHeight() / 2F), Color.WHITE
            )
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onKey(keyCode: Int) {}

}