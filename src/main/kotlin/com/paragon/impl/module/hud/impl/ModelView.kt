package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil

/**
 * @author Surge
 * @since 26/12/2022
 */
object ModelView : HUDModule("ModelView", "Draws the player model to the screen", { 50f }, { 60f }) {

    override fun draw() {
        RenderUtil.drawEntity(x + (width.invoke() / 2), y + (height.invoke() - 2), 1f, mc.player)
    }

}