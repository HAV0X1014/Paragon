package com.paragon.impl.ui.windows

import com.paragon.Paragon
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.BlurUtil
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.util.math.Vec2f
import java.awt.Color

/**
 * @author Surge
 * @since 27/07/2022
 */
abstract class Window(val title: String, var x: Float, var y: Float, var width: Float, var height: Float, var grabbableHeight: Float) {

    private var lastPosition = Vec2f(0f, 0f)
    private var dragging = false

    open fun scroll(mouseX: Int, mouseY: Int, mouseDelta: Int): Boolean {
        return false
    }

    open fun draw(mouseX: Int, mouseY: Int, mouseDelta: Int) {
        if (dragging) {
            x = mouseX - lastPosition.x
            y = mouseY - lastPosition.y
        }

        RenderUtil.drawRect(x, y + grabbableHeight, width, height - grabbableHeight, Color(0, 0, 0, 120))

        if (ClickGUI.blur.value) {
            BlurUtil.blur(x, y + grabbableHeight, width, height - grabbableHeight, ClickGUI.intensity.value)
        }

        RenderUtil.drawRect(x, y, width, grabbableHeight, Colours.mainColour.value)

        RenderUtil.pushScissor(x, y, width, 16f)

        FontUtil.drawStringWithShadow(title, x + 3, y + 4, Color.WHITE)

        RenderUtil.scaleTo((x + width - 7f) - FontUtil.font.getStringWidth("X"), y + 1, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawIcon(FontUtil.Icon.CLOSE, (x + width - 7f) - FontUtil.font.getStringWidth("X"), y + 1, Color.WHITE)
        }

        RenderUtil.popScissor()

        RenderUtil.drawBorder(x, y, width, height, 0.5f, Colours.mainColour.value)
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, click: Click): Boolean {
        if (mouseX.toFloat() in x + width - FontUtil.getStringWidth("X") - 5..x + width && mouseY.toFloat() in y..y + grabbableHeight) {
            Paragon.INSTANCE.configurationGUI.removeBuffer.add(this)
            return true
        }

        if (click == Click.LEFT && mouseX.toFloat() in x..x + width && mouseY.toFloat() in y..y + grabbableHeight) {
            dragging = true
            lastPosition = Vec2f(mouseX - x, mouseY - y)

            return true
        }

        return false
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, click: Click) {
        dragging = false
    }

    open fun keyTyped(character: Char, keyCode: Int) {}

}