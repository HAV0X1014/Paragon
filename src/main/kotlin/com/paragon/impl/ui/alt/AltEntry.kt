package com.paragon.impl.ui.alt

import com.paragon.impl.managers.alt.Alt
import com.paragon.util.isHovered
import com.paragon.util.render.RenderUtil.drawRect
import com.paragon.util.render.font.FontUtil.drawCenteredString
import java.awt.Color

class AltEntry(val alt: Alt, var offset: Float) {

    fun drawAlt(screenWidth: Int) {
        drawRect(
            0f,
            offset,
            screenWidth.toFloat(),
            20f,
            if (AltManagerGUI.selectedAltEntry === this) Color(238, 238, 239, 150) else Color(0, 0, 0, 150)
        )

        drawCenteredString(alt.email, screenWidth / 2f, offset + 7, Color.WHITE)
    }

    fun clicked(mouseX: Int, mouseY: Int, screenWidth: Int) {
        if (isHovered(0f, offset, screenWidth.toFloat(), 20f, mouseX, mouseY)) {
            alt.login()
        }
    }

}