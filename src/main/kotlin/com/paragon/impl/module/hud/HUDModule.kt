package com.paragon.impl.module.hud

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.client.Editor
import com.paragon.impl.ui.util.Click
import com.paragon.util.isHovered
import com.paragon.util.mc
import com.paragon.util.roundToNearest
import net.minecraft.client.gui.ScaledResolution

/**
 * @author Surge
 * @since 24/12/2022
 */
abstract class HUDModule(name: String, description: String, val width: () -> Float, val height: () -> Float) : Module(name, Category.HUD, description) {

    var x: Float = 50f
    var y: Float = 50f

    private var lastX = 0F
    private var lastY = 0F

    var dragging = false

    abstract fun draw()

    open fun dummy() {
        draw()
    }

    fun updatePosition(mouseX: Int, mouseY: Int) {
        if (dragging) {
            val sr = ScaledResolution(mc)
            val newX = mouseX - lastX
            val newY = mouseY - lastY

            x = newX
            y = newY

            val centerX = newX + width.invoke() / 2f
            val centerY = newY + height.invoke() / 2f

            if (centerX > sr.scaledWidth / 2f - 5 && centerX < sr.scaledWidth / 2f + 5) {
                x = sr.scaledWidth / 2f - width.invoke() / 2f
            }

            if (centerY > sr.scaledHeight / 2f - 5 && centerY < sr.scaledHeight / 2f + 5) {
                y = sr.scaledHeight / 2f - height.invoke() / 2f
            }

            x = x.roundToNearest(Editor.snap.value).toFloat()
            y = y.roundToNearest(Editor.snap.value).toFloat()
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (isHovered(x, y, width.invoke(), height.invoke(), mouseX, mouseY)) {
            when (Click.getClick(mouseButton)) {
                Click.LEFT -> {
                    lastX = mouseX - x
                    lastY = mouseY - y
                    dragging = true
                }

                Click.RIGHT -> {
                    if (isEnabled) {
                        toggle()
                        return true
                    }
                }

                else -> {}
            }
        }

        return false
    }

}