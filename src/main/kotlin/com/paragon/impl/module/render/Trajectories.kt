package com.paragon.impl.module.render

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.IRenderManager
import com.paragon.util.anyNull
import com.paragon.util.entity.EntityUtil
import com.paragon.util.mc
import com.paragon.util.render.ColourUtil.glColour
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import net.minecraft.item.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object Trajectories : Module("Trajectories", Category.RENDER, "Shows where projectiles will land") {

    private val line = Setting(
        "Line", true
    ) describedBy "Render a line to the projectile's destination"

    private val lineColour = Setting(
        "LineColour", Color(185, 17, 255)
    ) describedBy "The colour of the line" subOf line

    private val lineWidth = Setting(
        "LineWidth", 1.0f, 0.1f, 3.0f, 0.1f
    ) describedBy "The width of the line" subOf line

    private val box = Setting(
        "Box", true
    ) describedBy "Render a box at the projectile's destination"

    private val fill = Setting(
        "Fill", true
    ) describedBy "Fill the box at the end of the line" subOf box

    private val outline = Setting(
        "Outline", true
    ) describedBy "Outline the box at the end of the line" subOf box

    private val boxColour = Setting(
        "BoxColour", Color(185, 17, 255, 130)
    ) describedBy "The colour of the box at the end of the line" subOf box visibleWhen { fill.value || outline.value }

    private val bow = Setting(
        "Bow", true
    ) describedBy "Draw the trajectory of the bow"

    private val snowball = Setting(
        "Snowball", true
    ) describedBy "Draw the trajectory of snowballs"

    private val egg = Setting(
        "Egg", true
    ) describedBy "Draw the trajectory of eggs"

    private val exp = Setting(
        "EXP", true
    ) describedBy "Draw the trajectory of EXP bottles"

    private val potion = Setting(
        "Potion", true
    ) describedBy "Draw the trajectory of splash potions"

    override fun onRender3D() {
        if (mc.anyNull) {
            return
        }

        val stack = mc.player.heldItemMainhand

        // Check the item we are holding is a projectile (or a bow) and that projectile is enabled
        if (stack.item is ItemBow && bow.value || stack.item is ItemSnowball && snowball.value || stack.item is ItemEgg && egg.value || stack.item is ItemSplashPotion && potion.value || stack.item is ItemExpBottle && exp.value) {
            // If we are holding a bow, make sure we are charging
            if (stack.item is ItemBow && !mc.player.isHandActive) {
                return
            }

            // Original arrow position
            var position = Vec3d(
                mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * ((mc as IMinecraft).hookGetTimer() ?: return).renderPartialTicks - cos(
                    Math.toRadians(mc.player.rotationYaw.toDouble()).toFloat().toDouble()
                ) * 0.16f, mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * ((mc as IMinecraft).hookGetTimer() ?: return).renderPartialTicks + mc.player.getEyeHeight() - 0.15, mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * ((mc as IMinecraft).hookGetTimer() ?: return).renderPartialTicks - sin(
                    Math.toRadians(mc.player.rotationYaw.toDouble()).toFloat().toDouble()
                ) * 0.16f
            )

            // Original arrow velocity
            var velocity = Vec3d(
                -sin(Math.toRadians(mc.player.rotationYaw.toDouble())) * cos(Math.toRadians(mc.player.rotationPitch.toDouble())) * if (stack.item is ItemBow) 1.0f else 0.4f,
                -sin(Math.toRadians(mc.player.rotationPitch.toDouble())) * if (stack.item is ItemBow) 1.0f else 0.4f,
                cos(Math.toRadians(mc.player.rotationYaw.toDouble())) * cos(Math.toRadians(mc.player.rotationPitch.toDouble())) * if (stack.item is ItemBow) 1.0f else 0.4f
            )

            // Motion factor
            val motion = sqrt(velocity.x * velocity.x + velocity.y * velocity.y + velocity.z * velocity.z)

            // New velocity
            velocity = Vec3d(velocity.x / motion, velocity.y / motion, velocity.z / motion)

            // If we are holding a bow
            velocity = if (stack.item is ItemBow) {
                // Get the charge power
                val power = MathHelper.clamp(
                    ((72000 - mc.player.itemInUseCount) / 20.0f * ((72000 - mc.player.itemInUseCount) / 20.0f) + (72000 - mc.player.itemInUseCount) / 20.0f * 2.0f) / 3.0f, 0f, 1f
                ) * 3

                // Set velocity
                Vec3d(velocity.x * power, velocity.y * power, velocity.z * power)
            }
            else {
                Vec3d(velocity.x * 1.5, velocity.y * 1.5, velocity.z * 1.5)
            }

            // Check we want to draw the line
            if (line.value) {
                // GL render 3D
                glPushMatrix()
                glDisable(GL_TEXTURE_2D)
                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                glDisable(GL_DEPTH_TEST)
                glDepthMask(false)
                glEnable(GL_LINE_SMOOTH)

                // Set line width
                glLineWidth(lineWidth.value)

                lineColour.value.glColour()

                glBegin(GL_LINE_STRIP)

                // Add vertices to the line whilst we haven't hit a target
                for (i in 0..999) {
                    // Add vertex
                    glVertex3d(
                        position.x - (mc.renderManager as IRenderManager).hookGetRenderPosX(),
                        position.y - (mc.renderManager as IRenderManager).hookGetRenderPosY(),
                        position.z - (mc.renderManager as IRenderManager).hookGetRenderPosZ()
                    )

                    // Move position
                    position = Vec3d(
                        position.x + velocity.x * 0.1, position.y + velocity.y * 0.1, position.z + velocity.z * 0.1
                    )

                    velocity = Vec3d(
                        velocity.x, velocity.y - (if (stack.item is ItemBow) 0.05 else if (stack.item is ItemPotion) 0.4 else if (stack.item is ItemExpBottle) 0.1 else 0.03) * 0.1, velocity.z
                    )

                    // Check if we hit a target
                    val result = mc.world.rayTraceBlocks(
                        EntityUtil.getInterpolatedPosition(mc.player).add(
                            Vec3d(0.0, mc.player.getEyeHeight().toDouble(), 0.0)
                        ), Vec3d(position.x, position.y, position.z)
                    )
                    if (result != null) {
                        break
                    }
                }

                // Stop adding vertices
                glEnd()

                // Disable GL render 3D
                glDisable(GL_BLEND)
                glEnable(GL_TEXTURE_2D)
                glEnable(GL_DEPTH_TEST)
                glDepthMask(true)
                glDisable(GL_LINE_SMOOTH)
                glPopMatrix()
            }

            // Check we want to draw the box
            if (box.value) {
                // Get highlight bb
                val bb = AxisAlignedBB(
                    position.x - (mc.renderManager as IRenderManager).hookGetRenderPosX() - 0.25,
                    position.y - (mc.renderManager as IRenderManager).hookGetRenderPosY() - 0.25,
                    position.z - (mc.renderManager as IRenderManager).hookGetRenderPosZ() - 0.25,
                    position.x - (mc.renderManager as IRenderManager).hookGetRenderPosX() + 0.25,
                    position.y - (mc.renderManager as IRenderManager).hookGetRenderPosY() + 0.25,
                    position.z - (mc.renderManager as IRenderManager).hookGetRenderPosZ() + 0.25
                )

                // Draw filled box
                if (fill.value) {
                    RenderBuilder().boundingBox(bb).inner(boxColour.value).type(BoxRenderMode.FILL)

                        .start()

                        .blend(true).depth(true).texture(true)

                        .build(false)
                }

                // Draw outline box
                if (outline.value) {
                    RenderBuilder().boundingBox(bb).outer(boxColour.value.integrateAlpha(255f)).type(BoxRenderMode.OUTLINE)

                        .start()

                        .blend(true).depth(true).texture(true).lineWidth(lineWidth.value)

                        .build(false)
                }
            }
        }
    }
}