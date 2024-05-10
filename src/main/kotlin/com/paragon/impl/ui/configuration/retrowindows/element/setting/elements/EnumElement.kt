package com.paragon.impl.ui.configuration.retrowindows.element.setting.elements

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.impl.ui.configuration.retrowindows.element.setting.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.string.StringUtil
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 */
class EnumElement(parent: ModuleElement, setting: Setting<Enum<*>>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Enum<*>>(parent, setting, x, y, width, height) {

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x + 3, y + 3, width - 4, height - 4, Color(100, 100, 100))
        RenderUtil.drawRect(x + 2, y + 2, width - 4, height - 4, Color(130, 130, 130))

        RenderUtil.drawHorizontalGradientRect(x + 2, y + 2, width - 4, height - 4, Colours.mainColour.value, if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter() else Colours.mainColour.value)

        glScalef(0.8f, 0.8f, 0.8f)

        val scaleFactor = 1 / 0.8f
        FontUtil.drawStringWithShadow(setting.name, (x + 5) * scaleFactor, (y + 5f) * scaleFactor, Color.WHITE)

        val valueX = (x + width - FontUtil.getStringWidth(StringUtil.getFormattedText(setting.value)) * 0.8f - 5) * scaleFactor

        FontUtil.drawStringWithShadow(StringUtil.getFormattedText(setting.value), valueX, (y + 5f) * scaleFactor, Color(190, 190, 190))

        glScalef(scaleFactor, scaleFactor, scaleFactor)

        if (expanded.getAnimationFactor() > 0) {
            var yOffset = 0f

            val scissorY = MathHelper.clamp(y + height, parent.parent.y + parent.parent.height, ((parent.parent.y + parent.parent.scissorHeight) - getSubSettingHeight() * expanded.getAnimationFactor().toFloat()) + height).toDouble()
            val scissorHeight = MathHelper.clamp(getSubSettingHeight().toDouble() * expanded.getAnimationFactor(), 0.0, parent.parent.scissorHeight.toDouble())

            RenderUtil.pushScissor(x, scissorY.toFloat(), width, scissorHeight.toFloat())

            subSettings.forEach {
                it.x = x + 2
                it.y = y + height + yOffset

                it.draw(mouseX, mouseY, mouseDelta)

                yOffset += it.getTotalHeight()
            }

            RenderUtil.popScissor()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && y in parent.parent.y + parent.parent.height..parent.parent.y + parent.parent.height + parent.parent.scissorHeight) {
            if (click == Click.LEFT) {
                setting.setValue(setting.nextMode)
                Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
            }
            else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

}