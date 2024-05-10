package com.paragon.impl.ui.configuration

import com.paragon.Paragon
import com.paragon.impl.module.client.ClickGUI
import net.minecraft.client.Minecraft

/**
 * @author Surge
 * @since 27/07/2022
 */
abstract class GuiImplementation {

    var width = 0f
    var height = 0f

    open fun initGui() {}

    abstract fun drawScreen(mouseX: Int, mouseY: Int, mouseDelta: Int)
    abstract fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
    abstract fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)
    abstract fun keyTyped(typedChar: Char, keyCode: Int)

    open fun onGuiClosed() {
        Paragon.INSTANCE.storageManager.saveModules("current")
    }

    open fun doesGuiPauseGame(): Boolean = ClickGUI.pause.value

    fun drawDefaultBackground() {
        Minecraft.getMinecraft().currentScreen?.drawDefaultBackground()
    }

}