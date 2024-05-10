package com.paragon.impl.module.render

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.entity.EntityUtil
import com.paragon.util.mc
import com.paragon.util.player.PlayerUtil
import com.paragon.util.render.ColourUtil.glColour
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author Surge, SooStrator1136
 */
object ChinaHat : Module("ChinaHat", Category.RENDER, "-69420 social credit :((") {

    // Colours
    private val topColour = Setting(
        "TopColour", Color(185, 17, 255, 180)
    ) describedBy "The top colour of the hat"

    private val bottomColour = Setting(
        "BottomColour", Color(185, 17, 255, 180)
    ) describedBy "The bottom colour of the hat"

    // Settings
    private val firstPerson = Setting(
        "FirstPerson", false
    ) describedBy "Render the hat in first person"

    private val others = Setting(
        "Others", true
    ) describedBy "Render the hat on other players"

    override fun onRender3D() {
        if (mc.anyNull) {
            return
        }

        mc.world?.playerEntities?.forEach {
            // We don't want to render the hat
            if (it === mc.player && !firstPerson.value && mc.gameSettings.thirdPersonView == 0 || !others.value && it !== mc.player) {
                return
            }

            // Render the hat
            renderHat(it)
        }
    }

    private fun renderHat(player: EntityPlayer) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_POINT_SMOOTH)
        glEnable(GL_BLEND)
        glShadeModel(GL_SMOOTH)
        GlStateManager.disableCull()
        glBegin(GL_TRIANGLE_STRIP)

        // Get the vector to start drawing the hat
        val vec = EntityUtil.getInterpolatedPosition(player).add(
            Vec3d(
                -mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY + player.getEyeHeight() + 0.5 + if (player.isSneaking) -0.2 else 0.0, -mc.renderManager.viewerPosZ
            )
        )

        // Change vec if elytra flying
        if (player.isElytraFlying) {
            vec.add(Vec3d(PlayerUtil.forward(2.0).x, -0.8, PlayerUtil.forward(2.0).y))
        }

        var i = 0.0
        while (i < Math.PI * 2) {
            i += Math.PI * 4 / 128 // There is no classic for loop in kt

            // Set bottom colour
            bottomColour.value.glColour()

            // Add bottom point
            glVertex3d(vec.x + 0.65 * cos(i), vec.y - 0.25, vec.z + 0.65 * sin(i))

            // Set top colour
            topColour.value.glColour()

            // Add top point
            glVertex3d(vec.x, vec.y, vec.z)
        }

        glEnd()
        glShadeModel(GL_FLAT)
        glDepthMask(true)
        glEnable(GL_LINE_SMOOTH)
        GlStateManager.enableCull()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_POINT_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

}