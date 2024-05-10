package com.paragon.impl.ui.configuration.retrowindows

import com.paragon.Paragon
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.retrowindows.element.CategoryWindow
import com.paragon.impl.ui.configuration.shared.Panel
import com.paragon.impl.ui.util.Click
import com.paragon.impl.module.Category
import com.paragon.util.render.RenderUtil
import com.paragon.util.string.StringUtil
import java.awt.Color

/**
 * @author Surge
 */
class Windows98 : GuiImplementation() {

    private val windows = ArrayList<Panel>()

    init {
        var x = 20f

        Category.values().forEach {
            windows.add(CategoryWindow(it, x, 20f, 100f, 16f))

            x += 105f
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        var tooltipName = ""
        var tooltipContent = ""

        // Render the last window in the list over the others etc
        windows.reverse()

        windows.forEach { window ->
            window.draw(mouseX.toFloat(), mouseY.toFloat(), mouseDelta)

            if (window is CategoryWindow && window.tooltipName.isNotEmpty()) {
                tooltipName = window.tooltipName
                tooltipContent = window.tooltipContent
            }
        }

        windows.reverse()

        if (tooltipName.isNotEmpty() && tooltipContent.isNotEmpty()) {
            val text = StringUtil.wrap(tooltipContent, 30)
            val width = FontUtil.getStringWidth(text) + 6f
            val height = 18f + (text.split(System.lineSeparator()).size * FontUtil.getHeight())

            RenderUtil.drawRect(mouseX + 8f, mouseY + 8f, width, height, Color(70, 70, 70))
            RenderUtil.drawRect(mouseX + 7f, mouseY + 7f, width, height, Color(148, 148, 148))

            FontUtil.drawStringWithShadow(tooltipName, mouseX + 9f, mouseY + 12f, Color.WHITE)
            FontUtil.drawStringWithShadow(text, mouseX + 9f, mouseY + 12f + FontUtil.getHeight(), Color.WHITE)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        windows.forEach { it.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton)) }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        windows.forEach { it.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton)) }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        windows.forEach { it.keyTyped(typedChar, keyCode) }
    }

    override fun onGuiClosed() {
        Paragon.INSTANCE.storageManager.saveModules("current")
    }

    override fun doesGuiPauseGame() = ClickGUI.pause.value

}