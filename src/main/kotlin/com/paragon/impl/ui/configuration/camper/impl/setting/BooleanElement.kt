package com.paragon.impl.ui.configuration.camper.impl.setting

import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.camper.impl.ModuleElement
import com.paragon.impl.ui.configuration.camper.impl.SettingElement
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import java.awt.Color

class BooleanElement(parent: ModuleElement, setting: Setting<Boolean>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Boolean>(parent, setting, x, y, width, height) {

    private val enabled = Animation({ ClickGUI.animationSpeed.value }, false, { ClickGUI.easing.value })

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        enabled.state = setting.value

        RenderUtil.drawRect(x, y, width, height, (hover.getColour().fade(Colours.mainColour.value.fade(Colours.mainColour.value.darker(), hover.getAnimationFactor()), enabled.getAnimationFactor())))

        RenderUtil.scaleTo(x + 3, y + 5f, 0f, 0.7, 0.7, 0.7) {
            FontUtil.drawStringWithShadow(setting.name, x + 3, y + 3f, Color.WHITE)
        }

        drawSettings(mouseX, mouseY, mouseDelta)
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (hover.state) {
            if (click == Click.LEFT) {
                setting.setValue(!setting.value)
            } else if (click == Click.RIGHT) {
                expanded.state = !expanded.state
            }
        }
    }

}