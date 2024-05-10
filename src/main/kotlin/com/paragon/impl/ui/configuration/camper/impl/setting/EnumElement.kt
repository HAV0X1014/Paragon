package com.paragon.impl.ui.configuration.camper.impl.setting

import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.camper.impl.ModuleElement
import com.paragon.impl.ui.configuration.camper.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.string.StringUtil
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

class EnumElement(parent: ModuleElement, setting: Setting<Enum<*>>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Enum<*>>(parent, setting, x, y, width, height) {

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x, y, width, height, hover.getColour())

        RenderUtil.scaleTo(x + 3, y + 5.5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 3f, Color.WHITE)
        }

        run {
            glScalef(0.7f, 0.7f, 0.7f)

            val factor = 1 / 0.7f

            val valueX = (x + getRenderableWidth() - FontUtil.getStringWidth(StringUtil.getFormattedText(setting.value)) * 0.7f - if (getRenderableWidth() != width) 0 else 5) * factor

            FontUtil.drawStringWithShadow(StringUtil.getFormattedText(setting.value), valueX, (y + 3.5f) * factor, Color.GRAY)

            glScalef(factor, factor, factor)
        }

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            if (click == Click.LEFT) {
                setting.setValue(setting.nextMode)
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

}