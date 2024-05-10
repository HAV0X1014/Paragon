package com.paragon.impl.ui.configuration.phobos.element.setting.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.phobos.element.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 * @since 31/07/2022
 */
class BooleanElement(setting: Setting<Boolean>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Boolean>(setting, x, y, width, height) {

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        val hovered = isHovered(mouseX, mouseY)

        if (hovered && !setting.value) {
            RenderUtil.drawRect(x, y, width, height, Color(200, 200, 200, 150))
        }

        if (setting.value) {
            RenderUtil.drawRect(x, y, width, height, Colours.mainColour.value.integrateAlpha(if (hovered) 205f else 150f))
        }

        glScalef(0.85f, 0.85f, 0.85f).let {
            val factor = 1 / 0.85f

            FontUtil.drawStringWithShadow(setting.name, (x + 4) * factor, (y + 4) * factor, Color.WHITE)

            glScalef(factor, factor, factor)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.LEFT) {
            setting.setValue(!setting.value)
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

}