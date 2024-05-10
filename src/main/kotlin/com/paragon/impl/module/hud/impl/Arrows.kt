package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.util.mc
import com.paragon.util.player.RotationUtil
import com.paragon.util.render.RenderUtil
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * @author Surge
 * @since 27/12/2022
 */
object Arrows : Module("Arrows", Category.HUD, "Draws arrows pointing to other players to the screen") {

    override fun onRender2D() {
        val resolution = ScaledResolution(mc)

        mc.world.playerEntities.forEach {
            if (it == mc.player) {
                return@forEach
            }

            val vec = it.positionVector
            val rotation = RotationUtil.getRotationToVec3d(vec).x

            RenderUtil.rotate(rotation - mc.player.rotationYaw - 90f, resolution.scaledWidth / 2f, resolution.scaledHeight / 2f, 0f) {
                RenderUtil.drawArrow((resolution.scaledWidth / 2f) + 30.5f, resolution.scaledHeight / 2f, 6f, 10f, 1f, Color.WHITE, 0f)
            }
        }
    }

}