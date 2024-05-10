package com.paragon.impl.module.render

import com.paragon.impl.event.render.entity.RenderCrystalEvent
import com.paragon.impl.event.render.entity.RenderEntityEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.mc
import com.paragon.util.render.RubiksCrystalUtil
import me.surge.animation.Easing
import net.minecraft.client.Minecraft
import net.minecraft.client.model.ModelRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLiving
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Quaternion
import java.awt.Color
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin

/**
 * Rubik's crystals based off of rebane2001's and olliem5's Rubiks crystals
 *
 * otherwise
 * @author Surge
 */
object Chams : Module("Chams", Category.RENDER, "Shows entities through walls") {

    private val mode = Setting(
        "Mode", Mode.WIRE_MODEL
    ) describedBy "The render mode to use"

    private val width = Setting(
        "Width", 1f, 0.1f, 3f, 0.1f
    ) describedBy "The width of the lines" visibleWhen { mode.value != Mode.MODEL }

    // Entity filters
    private val players = Setting(
        "Players", true
    ) describedBy "Highlight players"

    private val mobs = Setting(
        "Mobs", true
    ) describedBy "Highlight mobs"

    private val passives = Setting(
        "Passives", true
    ) describedBy "Highlight passives"

    // Crystals
    private val crystals = Setting(
        "Crystals", true
    ) describedBy "Highlight crystals"

    private val bounce = Setting(
        "Bounce", false
    ) describedBy "Make the crystals bounce like they do in vanilla" subOf crystals

    private val scaleSetting = Setting(
        "Scale", 0.6f, 0.0f, 1f, 0.01f
    ) describedBy "The scale of the crystal" subOf crystals

    private val rubiks = Setting(
        "Rubik's Cube", false
    ) describedBy "Make end crystals look like Rubik's cubes" subOf crystals

    private val time = Setting(
        "Time", 400f, 200f, 1000f, 1f
    ) describedBy "The time it takes for a side to rotate" subOf crystals visibleWhen { rubiks.value }

    private val cube = Setting("Cube", true) describedBy "Render the crystal cube" subOf crystals

    private val glass = Setting(
        "Glass", true
    ) describedBy "Render the glass" subOf crystals

    // Render settings
    private val texture = Setting(
        "Texture", false
    ) describedBy "Render the entity's texture"

    private val lighting = Setting(
        "Lighting", true
    ) describedBy "Disables lighting"

    private val blend = Setting(
        "Blend", true
    ) describedBy "Enables blending"

    private val transparent = Setting(
        "Transparent", true
    ) describedBy "Enables transparency on models"

    private val depth = Setting(
        "Depth", true
    ) describedBy "Enables depth"

    private val walls = Setting(
        "Walls", true
    ) describedBy "Render entities through walls"

    private val colour = Setting(
        "Colour", Color(185, 17, 255, 85)
    ) describedBy "The colour of the highlight"

    private var rotating = 0
    private var lastTime: Long = 0
    private var cubeModel: ModelRenderer? = null
    private const val CUBELET_SCALE = 0.4

    @Listener
    fun onRenderEntity(event: RenderEntityEvent) {
        // Check entity is valid
        if (!isEntityValid(event.entity)) {
            return
        }

        // Cancel model render
        if (!texture.value) {
            event.cancel()
        }

        glPushMatrix()
        glPushAttrib(GL_ALL_ATTRIB_BITS)

        // Enable transparency
        if (transparent.value) {
            GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
        }

        // Disable texture
        if (!texture.value) {
            glDisable(GL_TEXTURE_2D)
        }

        val originalBlend = glIsEnabled(GL_BLEND)

        // Enable blend
        if (blend.value) {
            glEnable(GL_BLEND)
        }

        // Disable lighting
        if (lighting.value) {
            glDisable(GL_LIGHTING)
        }

        // Remove depth
        if (depth.value) {
            glDepthMask(false)
        }

        // Remove depth
        if (walls.value) {
            glDisable(GL_DEPTH_TEST)
        }

        when (mode.value) {
            Mode.WIRE -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
            Mode.WIRE_MODEL, Mode.MODEL -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
        }

        // Anti aliasing for smooth lines
        glEnable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        // Set line width
        glLineWidth(width.value)

        // Set colour
        glColor4f(
            colour.value.red / 255f, colour.value.green / 255f, colour.value.blue / 255f, colour.value.alpha / 255f
        )

        // Render model
        event.renderModel()

        // Re enable depth
        if (walls.value && mode.value != Mode.WIRE_MODEL) {
            glEnable(GL_DEPTH_TEST)
        }

        // Change polygon rendering mode
        if (mode.value == Mode.WIRE_MODEL) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        }

        glColor4f(
            colour.value.red / 255f, colour.value.green / 255f, colour.value.blue / 255f, if (mode.value == Mode.MODEL) colour.value.alpha / 255f else 1f
        )

        // Render model
        event.renderModel()

        // Re enable depth
        if (walls.value && mode.value == Mode.WIRE_MODEL) {
            glEnable(GL_DEPTH_TEST)
        }

        // Enable lighting
        if (lighting.value) {
            glEnable(GL_LIGHTING)
        }

        // Enable depth
        if (depth.value) {
            glDepthMask(true)
        }

        // Enable blending
        if (!originalBlend) {
            glDisable(GL_BLEND)
        }

        // Enable texture
        if (!texture.value) {
            glEnable(GL_TEXTURE_2D)
        }

        // Reset colour
        GlStateManager.color(1f, 1f, 1f, 1f)

        if (transparent.value) {
            GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
            GlStateManager.enableBlendProfile(GlStateManager.Profile.DEFAULT)
        }

        glPopAttrib()
        glPopMatrix()
    }

    @Listener
    fun onRenderCrystal(event: RenderCrystalEvent) {
        if (crystals.value) {
            // Cancel vanilla crystal render
            event.cancel()
            glPushMatrix()
            glPushAttrib(GL_ALL_ATTRIB_BITS)

            // Enable transparency
            if (transparent.value) {
                GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
            }

            // Disable texture
            if (!texture.value) {
                glDisable(GL_TEXTURE_2D)
            }

            // Enable blend
            if (blend.value) {
                glEnable(GL_BLEND)
            }

            // Disable lighting
            if (lighting.value) {
                glDisable(GL_LIGHTING)
            }

            // Remove depth
            if (depth.value) {
                glDepthMask(false)
            }

            // Remove depth
            if (walls.value) {
                glDisable(GL_DEPTH_TEST)
            }
            when (mode.value) {
                Mode.WIRE -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
                Mode.WIRE_MODEL, Mode.MODEL -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
            }

            // Anti aliasing for smooth lines
            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

            // Set line width
            glLineWidth(width.value)

            // Set colour
            glColor4f(
                colour.value.red / 255f, colour.value.green / 255f, colour.value.blue / 255f, colour.value.alpha / 255f
            )

            // Render crystal
            renderCrystal(event)

            // Re enable depth
            if (walls.value && mode.value != Mode.WIRE_MODEL) {
                glEnable(GL_DEPTH_TEST)
            }

            // Change polygon rendering mode
            if (mode.value == Mode.WIRE_MODEL) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
            }

            glColor4f(
                colour.value.red / 255f, colour.value.green / 255f, colour.value.blue / 255f, if (mode.value == Mode.MODEL) colour.value.alpha / 255f else 1f
            )

            if (mode.value == Mode.WIRE_MODEL) {
                renderCrystal(event)
            }

            // Re enable depth
            if (walls.value && mode.value == Mode.WIRE_MODEL) {
                glEnable(GL_DEPTH_TEST)
            }

            // Enable lighting
            if (lighting.value) {
                glEnable(GL_LIGHTING)
            }

            // Enable depth
            if (depth.value) {
                glDepthMask(true)
            }

            // Enable blending
            if (blend.value) {
                glDisable(GL_BLEND)
            }

            // Enable texture
            if (!texture.value) {
                glEnable(GL_TEXTURE_2D)
            }

            // Reset colour
            GlStateManager.color(1f, 1f, 1f, 1f)

            // Reset scale
            if (rubiks.value) {
                GlStateManager.scale(1 / 0.5f, 1 / 0.5f, 1 / 0.5f)
            }
            else {
                GlStateManager.scale(1 / scaleSetting.value, 1 / scaleSetting.value, 1 / scaleSetting.value)
            }

            // Enable transparency
            if (transparent.value) {
                GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL)
            }
            glPopAttrib()
            glPopMatrix()
        }
    }

    private fun renderCrystal(event: RenderCrystalEvent) {
        if (rubiks.value) {
            cubeModel = event.cube
            GlStateManager.pushMatrix()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.translate(0.0f, -0.5f, 0.0f)

            if (event.base != null) {
                event.base.render(event.scale)
            }

            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)

            if (event.base != null) {
                GlStateManager.translate(0.0f, 1.2f, 0.0f)
            }

            else {
                GlStateManager.translate(0.0f, 1.0f, 0.0f)
            }

            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)

            if (glass.value) {
                event.glass.render(event.scale)
            }

            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)

            if (glass.value) {
                event.glass.render(event.scale)
            }

            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)

            val CUBELET_SCALE = 0.5

            // Scale cubelets
            GlStateManager.scale(0.72, 0.72, 0.72)
            GlStateManager.scale(CUBELET_SCALE, CUBELET_SCALE, CUBELET_SCALE)
            event.scale = (event.scale * (CUBELET_SCALE * 2)).toFloat()
            val currentTime = Minecraft.getSystemTime()

            if (currentTime - time.value > lastTime) {
                val currentSide = RubiksCrystalUtil.cubeSides[rotating]
                val cubletsTemp = arrayOf(
                    RubiksCrystalUtil.cubeletStatus[currentSide[0]], RubiksCrystalUtil.cubeletStatus[currentSide[1]], RubiksCrystalUtil.cubeletStatus[currentSide[2]], RubiksCrystalUtil.cubeletStatus[currentSide[3]], RubiksCrystalUtil.cubeletStatus[currentSide[4]], RubiksCrystalUtil.cubeletStatus[currentSide[5]], RubiksCrystalUtil.cubeletStatus[currentSide[6]], RubiksCrystalUtil.cubeletStatus[currentSide[7]], RubiksCrystalUtil.cubeletStatus[currentSide[8]]
                )

                //Rotation direction
                RubiksCrystalUtil.cubeletStatus[currentSide[0]] = cubletsTemp[6]
                RubiksCrystalUtil.cubeletStatus[currentSide[1]] = cubletsTemp[3]
                RubiksCrystalUtil.cubeletStatus[currentSide[2]] = cubletsTemp[0]
                RubiksCrystalUtil.cubeletStatus[currentSide[3]] = cubletsTemp[7]
                RubiksCrystalUtil.cubeletStatus[currentSide[4]] = cubletsTemp[4]
                RubiksCrystalUtil.cubeletStatus[currentSide[5]] = cubletsTemp[1]
                RubiksCrystalUtil.cubeletStatus[currentSide[6]] = cubletsTemp[8]
                RubiksCrystalUtil.cubeletStatus[currentSide[7]] = cubletsTemp[5]
                RubiksCrystalUtil.cubeletStatus[currentSide[8]] = cubletsTemp[2]

                val trans = RubiksCrystalUtil.cubeSideTransforms[rotating]

                for (x in -1..1) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            if (x != 0 || y != 0 || z != 0) {
                                applyRotation(x, y, z, trans[0], trans[1], trans[2])
                            }
                        }
                    }
                }

                rotating = ThreadLocalRandom.current().nextInt(0, 5 + 1)
                lastTime = currentTime
            }

            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        if (x != 0 || y != 0 || z != 0) {
                            if (cube.value) {
                                drawCubeletStatic(event.scale, x, y, z)
                            }
                        }
                    }
                }
            }

            val trans = RubiksCrystalUtil.cubeSideTransforms[rotating]
            GlStateManager.pushMatrix()
            GlStateManager.translate(trans[0] * CUBELET_SCALE, trans[1] * CUBELET_SCALE, trans[2] * CUBELET_SCALE)

            val angle = Math.toRadians(
                Easing.EXPO_IN_OUT.ease(((currentTime - lastTime).toFloat() / time.value).toDouble())
            ).toFloat() * 90

            val xx = (trans[0] * sin((angle / 2).toDouble())).toFloat()
            val yy = (trans[1] * sin((angle / 2).toDouble())).toFloat()
            val zz = (trans[2] * sin((angle / 2).toDouble())).toFloat()
            val ww = cos((angle / 2).toDouble()).toFloat()

            val q = Quaternion(xx, yy, zz, ww)
            GlStateManager.rotate(q)

            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        if (x != 0 || y != 0 || z != 0) {
                            if (cube.value) {
                                drawCubeletRotating(event.scale, x, y, z)
                            }
                        }
                    }
                }
            }
        }
        else {
            GlStateManager.pushMatrix()
            GlStateManager.scale(2.0f, 2.0f, 2.0f)
            GlStateManager.translate(0.0f, -0.5f, 0.0f)

            if (event.base != null) {
                event.base.render(event.scale)
            }

            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.translate(0.0f, 0.8f + if (bounce.value) event.ageInTicks else 0f, 0.0f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleSetting.value, scaleSetting.value, scaleSetting.value)

            if (glass.value) {
                event.glass.render(event.scale)
            }

            GlStateManager.popMatrix()
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleSetting.value, scaleSetting.value, scaleSetting.value)

            if (glass.value) {
                event.glass.render(event.scale)
            }

            GlStateManager.popMatrix()
            GlStateManager.scale(0.875f, 0.875f, 0.875f)
            GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f)
            GlStateManager.rotate(event.limbSwingAmount, 0.0f, 1.0f, 0.0f)
            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleSetting.value, scaleSetting.value, scaleSetting.value)

            if (cube.value) {
                event.cube.render(event.scale)
            }
        }
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
    }

    private fun drawCubeletStatic(scale: Float, x: Int, y: Int, z: Int) {
        val id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1]

        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).anyMatch { i: Int -> i == id }) {
            return
        }

        drawCubelet(scale, x, y, z, id)
    }

    private fun drawCubeletRotating(scale: Float, x: Int, y: Int, z: Int) {
        val id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1]

        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).noneMatch { i: Int -> i == id }) {
            return
        }

        val transform = RubiksCrystalUtil.cubeSideTransforms[rotating]
        drawCubelet(scale, x - transform[0], y - transform[1], z - transform[2], id)
    }

    private fun applyRotation(x: Int, y: Int, z: Int, rX: Int, rY: Int, rZ: Int) {
        val id = RubiksCrystalUtil.cubeletLookup[x + 1][y + 1][z + 1]

        if (Arrays.stream(RubiksCrystalUtil.cubeSides[rotating]).noneMatch { i: Int -> i == id }) {
            return
        }

        val angle = Math.toRadians(90.0).toFloat()
        val xx = (rX * sin((angle / 2).toDouble())).toFloat()
        val yy = (rY * sin((angle / 2).toDouble())).toFloat()
        val zz = (rZ * sin((angle / 2).toDouble())).toFloat()
        val ww = cos((angle / 2).toDouble()).toFloat()
        RubiksCrystalUtil.cubeletStatus[id] = Quaternion.mul(Quaternion(xx, yy, zz, ww), RubiksCrystalUtil.cubeletStatus[id], null)
    }

    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    private fun drawCubelet(scale: Float, x: Int, y: Int, z: Int, id: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x * 0.5, y * 0.5, z * 0.5)
        GlStateManager.pushMatrix()
        GlStateManager.rotate(RubiksCrystalUtil.cubeletStatus[id])
        if (cube.value) {
            cubeModel!!.render(scale)
        }
        GlStateManager.popMatrix()
        GlStateManager.popMatrix()
    }

    /**
     * Checks if an entity is valid
     *
     * @param entityIn The entity to check
     * @return Is the entity valid
     */
    private fun isEntityValid(entityIn: Entity): Boolean {
        return entityIn is EntityPlayer && entityIn !== mc.player && players.value || entityIn is EntityLiving && entityIn !is EntityMob && passives.value || entityIn is EntityMob && mobs.value
    }

    enum class Mode {
        /**
         * Renders the model
         */
        MODEL,

        /**
         * Outlines the model
         */
        WIRE,

        /**
         * Renders and outlines the model
         */
        WIRE_MODEL
    }

}