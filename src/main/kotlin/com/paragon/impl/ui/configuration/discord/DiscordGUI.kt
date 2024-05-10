package com.paragon.impl.ui.configuration.discord

import com.paragon.Paragon
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.discord.category.CategoryBar
import com.paragon.impl.ui.configuration.discord.module.ModuleBar
import com.paragon.impl.ui.configuration.discord.settings.SettingsBar
import com.paragon.util.mc
import org.lwjgl.util.Rectangle
import java.awt.Color

/**
 * @author SooStrator1136
 */
class DiscordGUI : GuiImplementation() {

    private val parts = arrayOf(
        SettingsBar, ModuleBar, CategoryBar
    )

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        baseRect.setBounds(
            ((mc.currentScreen ?: return).width / 2) - 200, ((mc.currentScreen ?: return).height / 2) - 150, 400, 300
        )

        dWheel = mouseDelta

        for (renderable in parts) {
            renderable.render(mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (renderable in parts) {
            renderable.onClick(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (renderable in parts) {
            renderable.onRelease(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (renderable in parts) {
            renderable.onKey(keyCode)
        }
    }

    override fun onGuiClosed() {
        ModuleBar.scrollOffset = 0
        ModuleBar.focusedModule = null
        ModuleBar.shownModules.clear()
        ModuleBar.lastCopyTime = 0L
        SettingsBar.shownSettings.clear()
        SettingsBar.scrollOffset = 0
        Paragon.INSTANCE.configurationGUI.closeOnEscape = true

        super.onGuiClosed()
    }

    override fun doesGuiPauseGame() = ClickGUI.pause.value

    companion object {
        var dWheel = 0
            private set

        val baseRect = Rectangle()

        val userFieldBackground = Color(41, 43, 47)
        val userCopiedColor = Color(59, 165, 93)

        val categoryBarBackground = Color(32, 34, 37)
        val categoryTextBackground = Color(24, 25, 28)

        val channelBarBackground = Color(47, 49, 54)
        val channelTextColor = Color(142, 146, 151)
        val channelHoveredColor = Color(60, 63, 69)

        val mediaBackgroundBorder = Color(44, 46, 51)
        val mediaBackground = channelBarBackground
        val mediaProgressBackground = Color(19, 20, 22)
        val mediaProgress = Color(88, 101, 242)
        val mediaProgressbarBackground = Color(69, 70, 73)
        val mediaSize = Color(107, 110, 114)
        val mediaTitle = Color(11, 155, 198)
        val msgHovered = Color(50, 53, 59)
        val chatBackground = Color(54, 57, 63)
    }

}