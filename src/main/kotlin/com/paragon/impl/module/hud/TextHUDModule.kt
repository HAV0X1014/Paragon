package com.paragon.impl.module.hud

import com.paragon.impl.module.client.Colours
import com.paragon.util.render.font.FontUtil

/**
 * @author Surge
 * @since 24/12/2022
 */
open class TextHUDModule(name: String, description: String, val content: () -> String) : HUDModule(name, description, { FontUtil.getStringWidth(content.invoke()) + 2f }, { FontUtil.getHeight() + 2f }) {

    override fun draw() {
        FontUtil.drawStringWithShadow(content.invoke(), x + 1, y + 1, Colours.mainColour.value)
    }

}