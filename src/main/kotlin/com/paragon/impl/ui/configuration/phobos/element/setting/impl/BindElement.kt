package com.paragon.impl.ui.configuration.phobos.element.setting.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.phobos.element.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 * @since 31/07/2022
 */
class BindElement(setting: Setting<Bind>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Bind>(setting, x, y, width, height) {

    private var listening = false

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        val hovered = isHovered(mouseX, mouseY)

        if (hovered && !listening) {
            RenderUtil.drawRect(x, y, width, height, Color(200, 200, 200, 150))
        }

        if (listening) {
            RenderUtil.drawRect(x, y, width, height, Colours.mainColour.value.integrateAlpha(if (hovered) 205f else 150f))
        }

        glScalef(0.85f, 0.85f, 0.85f).let {
            val factor = 1 / 0.85f

            FontUtil.drawStringWithShadow(setting.name, (x + 4) * factor, (y + 4) * factor, Color.WHITE)

            val valueX = (x + width - FontUtil.getStringWidth(if (listening) "..." else setting.value.getButtonName()) * 0.85f - 3) * factor
            FontUtil.drawStringWithShadow(if (listening) "..." else setting.value.getButtonName(), valueX, (y + 4f) * factor, Color(190, 190, 190))

            glScalef(factor, factor, factor)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.LEFT) {
            listening = !listening
            return
        }

        if (listening) {
            setting.setValue(Bind(click.button, Bind.Device.MOUSE))
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            listening = false
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (listening) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                listening = false
                return
            }

            listening = false

            if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
                setting.setValue(Bind(0, Bind.Device.KEYBOARD))
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
                return
            }

            setting.setValue(Bind(keyCode, Bind.Device.KEYBOARD))
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

}