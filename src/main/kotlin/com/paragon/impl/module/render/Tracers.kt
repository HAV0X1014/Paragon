package com.paragon.impl.module.render

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.entity.EntityUtil
import com.paragon.util.entity.EntityUtil.isEntityAllowed
import com.paragon.util.entity.EntityUtil.isMonster
import com.paragon.util.entity.EntityUtil.isPassive
import com.paragon.util.mc
import com.paragon.util.render.ColourUtil.glColour
import com.paragon.util.render.ColourUtil.integrateAlpha
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11.*
import java.awt.Color

/**
 * @author Surge
 */
object Tracers : Module("Tracers", Category.RENDER, "Draws lines to entities in the world") {

    private val passive = Setting("Passives", true) describedBy "Draws lines to passive entities"

    private val passiveColour = Setting(
        "Colour", Color(0, 255, 0, 180)
    ) describedBy "The colour to render the passive tracers in" subOf passive visibleWhen { distance.value != Distance.COLOUR }

    private val mobs = Setting("Mobs", true) describedBy "Draws lines to monsters"

    private val mobColour = Setting(
        "Colour", Color(255, 0, 0, 180)
    ) describedBy "The colour to render the mob tracers in" subOf mobs visibleWhen { distance.value != Distance.COLOUR }

    private val players = Setting(
        "Players", true
    ) describedBy "Draws lines to players"

    private val playerColour = Setting(
        "Colour", Color(255, 255, 255, 180)
    ) describedBy "The colour to render the player tracers in" subOf players visibleWhen { distance.value != Distance.COLOUR }

    private val crystals = Setting(
        "Crystals", true
    ) describedBy "Draws lines to ender crystals"

    private val crystalColour = Setting(
        "Colour", Color(200, 0, 200, 180)
    ) describedBy "The colour to render the ender crystal tracers in" subOf crystals visibleWhen { distance.value != Distance.COLOUR }

    private val distance = Setting(
        "Distance", Distance.ALPHA
    ) describedBy "Change the colour depending on the distance to the entity"

    private val distanceDivider = Setting(
        "Divider", 1500f, 0f, 5000f, 100f
    ) describedBy "The divider to use when calculating the distance factor" subOf distance

    private val lineWidth = Setting("LineWidth", 0.5f, 0.1f, 2f, 0.1f) describedBy "How thick to render the lines"

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent?) {
        mc.world.loadedEntityList.forEach { entity: Entity ->
            if (entity.isEntityAllowed(players.value, mobs.value, passive.value) && entity !== mc.player || entity is EntityEnderCrystal && crystals.value) {
                val vec = EntityUtil.getInterpolatedPosition(entity)
                val x = vec.x - mc.renderManager.viewerPosX
                val y = vec.y - mc.renderManager.viewerPosY
                val z = vec.z - mc.renderManager.viewerPosZ

                val eyes = Vec3d(0.0, 0.0, 1.0).rotatePitch(-Math.toRadians(mc.player.rotationPitch.toDouble()).toFloat()).rotateYaw(
                    -Math.toRadians(
                        mc.player.rotationYaw.toDouble()
                    ).toFloat()
                )

                if (getColourByEntity(entity).alpha == 0) {
                    return
                }

                // Enable render 3D
                glDepthMask(false)
                glDisable(GL_DEPTH_TEST)
                glDisable(GL_ALPHA_TEST)
                glEnable(GL_BLEND)
                glDisable(GL_TEXTURE_2D)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                glEnable(GL_LINE_SMOOTH)
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
                glLineWidth(0.1f)

                // Colour line
                getColourByEntity(entity).glColour()

                // Set line width
                glLineWidth(lineWidth.value)
                glBegin(GL_LINE_STRIP)

                // Draw line
                glVertex3d(eyes.x, eyes.y + mc.player.getEyeHeight(), eyes.z)
                glVertex3d(x, y + entity.height / 2, z)
                glEnd()

                // Disable render 3D
                glDepthMask(true)
                glEnable(GL_DEPTH_TEST)
                glEnable(GL_TEXTURE_2D)
                glDisable(GL_BLEND)
                glEnable(GL_ALPHA_TEST)
                glDisable(GL_LINE_SMOOTH)

                // Reset colour
                glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            }
        }
    }

    /**
     * Gets the entity's colour
     *
     * @param entityIn The entity
     * @return The entity's colour
     */
    private fun getColourByEntity(entityIn: Entity): Color {
        var colour = passiveColour.value

        if (entityIn.isPassive()) {
            colour = passiveColour.value
        }
        else if (entityIn.isMonster()) {
            colour = mobColour.value
        }
        else if (entityIn is EntityPlayer) {
            colour = playerColour.value
        }
        else if (entityIn is EntityEnderCrystal) {
            colour = crystalColour.value
        }

        if (distance.value != Distance.OFF) {
            val factor = MathHelper.clamp(
                (mc.player.getDistanceSq(entityIn) / distanceDivider.value).toFloat(), 0f, 1f
            )

            colour = if (distance.value == Distance.COLOUR) {
                Color(1f - factor, factor, 0f).integrateAlpha(colour.alpha.toFloat())
            }
            else {
                colour.integrateAlpha(255f * (1 - factor))
            }
        }

        return colour
    }

    enum class Distance {
        /**
         * Red -> Green depending on distance
         */
        COLOUR,

        /**
         * Transparency based on distance
         */
        ALPHA,

        /**
         * Don't do anything
         */
        OFF
    }

}