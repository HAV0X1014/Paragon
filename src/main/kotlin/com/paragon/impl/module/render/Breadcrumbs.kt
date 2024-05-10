package com.paragon.impl.module.render

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.PlayerUtil
import com.paragon.util.render.ColourUtil.glColour
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

/**
 * @author Surge
 */
@Aliases(["Trails"])
object Breadcrumbs : Module("Breadcrumbs", Category.RENDER, "Draws a trail behind you") {

    private val infinite = Setting("Infinite", false) describedBy "Breadcrumbs last forever"
    private val lifespanValue = Setting("Lifespan", 100f, 1f, 1000f, 1f) describedBy "The lifespan of the positions in ticks" visibleWhen { !infinite.value }
    private val lineWidth = Setting("LineWidth", 1f, 0.1f, 5f, 0.1f) describedBy "The width of the lines"
    private val colour = Setting("Colour", Color(185, 17, 255)) describedBy "The colour of the breadcrumbs"
    private val rainbow = Setting("Rainbow", true) describedBy "Makes the trail a rainbow"

    private val positions = LinkedList<Position>()
    private var colourHue = 0

    override fun onDisable() {
        // Clear positions when we disable
        positions.clear()
    }

    override fun onTick() {
        if (mc.anyNull || mc.player.ticksExisted <= 20) {
            // We may have just loaded into a world, so we need to clear the positions
            positions.clear()
            return
        }

        // Create position
        val pos = Position(
            Vec3d(
                mc.player.lastTickPosX,
                mc.player.lastTickPosY,
                mc.player.lastTickPosZ
            ),
            Color(
                Color.HSBtoRGB(
                    colourHue / 360f,
                    1f, 1f
                )
            )
        )

        if (PlayerUtil.isMoving || mc.player.posY != mc.player.lastTickPosY) {
            colourHue++
        }

        // Add position
        positions.add(pos)

        // Update positions
        positions.forEach { it.update() }

        // Remove old positions
        positions.removeIf { !it.isAlive && !infinite.value }
    }

    override fun onRender3D() {
        glPushMatrix()

        // GL stuff
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glLineWidth(lineWidth.value)

        // Disable lighting
        mc.entityRenderer.disableLightmap()

        glBegin(GL_LINE_STRIP)

        for (pos in positions) {
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            (if (rainbow.value) pos.colour else colour.value).glColour()

            glVertex3d(pos.position.x - renderPosX, pos.position.y - renderPosY, pos.position.z - renderPosZ)
        }

        // Reset colour
        glColor4d(1.0, 1.0, 1.0, 1.0)

        // End GL
        glEnd()
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    internal class Position(val position: Vec3d, val colour: Color) {

        // Position's lifespan
        private var lifespan = lifespanValue.value.toLong()

        /**
         * Decreases the lifespan of the position
         */
        fun update() {
            lifespan--
        }

        /**
         * Checks if the position is alive
         *
         * @return If the position is alive
         */
        val isAlive: Boolean
            get() = lifespan > 0
    }

}