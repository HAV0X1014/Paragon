package com.paragon.impl.ui.windows.impl

import com.paragon.Paragon
import com.paragon.impl.ui.util.Click
import com.paragon.impl.ui.windows.Window
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File

/**
 * shitcode lul
 * @author Surge
 * @since 27/07/2022
 */
class ConfigWindow(x: Float, y: Float, width: Float, height: Float, grabbableHeight: Float) : Window("Configs", x, y, width, height, grabbableHeight) {

    private val configsList: ArrayList<ConfigElement> = arrayListOf()

    private val saveButton = ConfigButton("Save", {
        val name = configNameElement.input

        if (name.isNotEmpty()) {
            Paragon.INSTANCE.storageManager.saveModules(name)
            configNameElement.input = ""
            configNameElement.listening = false
        }
        else {
            configNameElement.flash = true
        }

    }, x, y, width, 16f)

    private val configNameElement = ConfigTextField(x, y + 16f, width, 16f)

    private var scroll = 0f

    init {
        val configDirectory = File("paragon${File.separator}configs")

        for (file in configDirectory.list()!!) {
            configsList.add(ConfigElement(file, x, y, width, height))
        }
    }

    override fun scroll(mouseX: Int, mouseY: Int, mouseDelta: Int): Boolean {
        if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in (y + 16f)..(y + height - 16f)) {
            if (mouseDelta != 0) {
                scroll += 18 * if (mouseDelta > 0) 1 else -1
                return true
            }
        }

        return super.scroll(mouseX, mouseY, mouseDelta)
    }

    override fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        if (scroll > 0) {
            scroll = 0f
        }

        saveButton.x = x + (width / 2f) + 2
        saveButton.y = y + height - 19f
        saveButton.width = (width / 2f) - 5

        saveButton.draw(mouseX, mouseY)

        configNameElement.x = x + 3
        configNameElement.y = y + height - 19f
        configNameElement.width = (width / 2f) - 3

        configNameElement.draw(mouseX, mouseY)

        val configDirectory = File("paragon${File.separator}configs")

        for (file in configDirectory.list()!!) {
            if (!configsList.any { it.name == file }) {
                configsList.add(ConfigElement(file, x, y, width, height))
            }
        }

        configsList.sortBy { it.name != "current" }

        configsList.removeIf { it.remove || !configDirectory.list()?.contains(it.name)!! }

        RenderUtil.pushScissor(x, y + grabbableHeight + 1, width, height - (grabbableHeight * 2) - 3)

        if (configsList.isNotEmpty()) {
            val last = configsList[configsList.size - 1]

            if (last.y + last.height < y + height - 19f) {
                scroll++
            }
        }

        var offset = 0f
        configsList.forEach {
            it.x = x + 3
            it.y = (y + grabbableHeight + 3f) + offset + scroll
            it.width = width - 6f
            it.height = 16f

            it.draw(mouseX, mouseY)

            offset += it.height + 2f
        }

        RenderUtil.popScissor()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
        if (mouseY.toFloat() in y + grabbableHeight..y + (height - 19f)) {
            configsList.forEach {
                it.clicked(mouseX, mouseY, click)
            }
        }

        saveButton.clicked(mouseX, mouseY, click)
        configNameElement.clicked(mouseX, mouseY)

        // dragging
        val superValue = super.mouseClicked(mouseX, mouseY, click)

        if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + grabbableHeight + height) {
            return true
        }

        return superValue
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        configNameElement.keyTyped(character, keyCode)
    }

    class ConfigElement(val name: String, var x: Float, var y: Float, var width: Float, var height: Float) {
        var remove = false

        fun draw(mouseX: Int, mouseY: Int) {
            val hovered = mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height

            RenderUtil.drawRect(x, y, width, height, if (hovered) Color(0, 0, 0, 120) else Color(0, 0, 0, 150))
            FontUtil.drawStringWithShadow(name, x + 3, y + 4, Color.WHITE)

            RenderUtil.drawRect(x + width - 16f, y, 16f, height, if (hovered) Color(0, 0, 0, 120) else Color(0, 0, 0, 150))

            RenderUtil.scaleTo(x + width - 12.5f, y + 2f, 0f, 0.6, 0.6, 0.6) {
                FontUtil.drawIcon(
                    FontUtil.Icon.BIN,
                    x + width - 12.5f,
                    y + 2f,
                    if (mouseX.toFloat() in x + width - 9f..x + width && mouseY.toFloat() in y..y + height) Color.RED else Color.WHITE
                )
            }
        }

        fun clicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
            if (mouseX.toFloat() in x + width - 9f..x + width && mouseY.toFloat() in y..y + height) {
                val file = File("paragon${File.separator}configs${File.separator}${name}.json")

                file.delete()

                return true
            }

            if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height) {
                if (click == Click.LEFT) {
                    Paragon.INSTANCE.storageManager.loadModules(name.replace(".json", ""))
                    return true
                }
            }

            return false
        }
    }

    class ConfigTextField(var x: Float, var y: Float, var width: Float, var height: Float) {

        var input = ""
        var listening = false
        var flash = false

        private val flashAnimation = Animation({ 250f }, false, { Easing.LINEAR })

        fun draw(mouseX: Int, mouseY: Int) {
            flashAnimation.state = flash

            if (flashAnimation.getAnimationFactor() == 1.0) {
                flash = false
            }

            val hovered = mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height

            RenderUtil.drawRect(x, y, width, height, if (hovered) Color(0, 0, 0, 120) else Color(0, 0, 0, 150))
            RenderUtil.drawBorder(x, y, width, height, 0.5f, Color.BLACK)

            RenderUtil.drawRect(x, y, width, height, Color(255, 0, 0, (255 * flashAnimation.getAnimationFactor()).toInt()))

            FontUtil.drawStringWithShadow(input + if (listening) "_" else "", x + 3, y + 4, Color.WHITE)
        }

        fun clicked(mouseX: Int, mouseY: Int) {
            if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height) {
                listening = !listening
            }
        }

        fun keyTyped(character: Char, keyCode: Int) {
            if (listening) {
                if (keyCode == Keyboard.KEY_BACK) {
                    if (input.isNotEmpty()) {
                        input = input.substring(0, input.length - 1)
                    }
                }
                else if (keyCode == Keyboard.KEY_RETURN) {
                    listening = false
                }
                else if (ChatAllowedCharacters.isAllowedCharacter(character)) {
                    input += character
                }
            }
        }

    }

    class ConfigButton(val name: String, val invoke: Runnable, var x: Float, var y: Float, var width: Float, var height: Float) {

        fun draw(mouseX: Int, mouseY: Int) {
            val hovered = mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height

            RenderUtil.drawRect(x, y, width, height, if (hovered) Color(0, 0, 0, 120) else Color(0, 0, 0, 150))
            RenderUtil.drawBorder(x, y, width, height, 0.5f, Color.BLACK)

            FontUtil.drawStringWithShadow(name, x + 3, y + 4, Color.WHITE)
        }

        fun clicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
            if (mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + height) {
                invoke.run()
            }

            return false
        }

    }

}