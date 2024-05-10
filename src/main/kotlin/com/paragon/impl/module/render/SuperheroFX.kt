package com.paragon.impl.module.render

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.combat.EntityAttackedEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontRenderer
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.glEnable
import java.awt.Color
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ThreadLocalRandom


/**
 * @author Surge
 * @since 18/11/2022
 */
object SuperheroFX : Module("SuperheroFX", Category.RENDER, "Renders superhero style popups when entities take damages") {

    private val length = Setting("Length", 400f, 100f, 500f, 10f) describedBy "The length of the popup animation"

    private val superheroFont = FontRenderer(FontUtil.getFont(javaClass.getResourceAsStream("/assets/paragon/font/superhero.ttf")!!, 40f))

    private val texts = arrayOf("POW", "KAPOW", "BOOM", "ZAP", "KABOOM")
    private val popups = CopyOnWriteArrayList<Popup>()
    private val random = Random()

    override fun onRender3D() {
        popups.forEach {
            it.render()
        }

        popups.removeIf { it.animation.getAnimationFactor() == 0.0 && !it.animation.state }
    }

    @Listener
    fun onEntityAttackedEvent(event: EntityAttackedEvent) {
        for (i in 0 until ThreadLocalRandom.current().nextInt(4)) {
            val offsetX = random.nextFloat() * 2
            val offsetY = random.nextFloat() * 2
            val offsetZ = random.nextFloat() * 2
            val text = texts[random.nextInt(texts.size)]

            popups.add(
                Popup(
                    event.entity.positionVector.addVector(
                        offsetX - 1.0,
                        event.entity.height + offsetY - 1.0,
                        offsetZ - 1.0
                    ), text, Color(Color.HSBtoRGB(random.nextFloat(), 1f, 1f))
                )
            )
        }
    }

    private class Popup(val vec: Vec3d, val text: String, val colour: Color) {
        val animation = Animation({ length.value }, false, Easing.CUBIC_IN_OUT)

        init {
            animation.state = true
        }

        fun render() {
            if (animation.state && animation.getAnimationFactor() == 1.0) {
                animation.state = false
            }

            RenderUtil.drawNametag(vec, false) {
                val width = superheroFont.getStringWidth(text).toFloat()

                RenderUtil.scaleTo(width / 2f, superheroFont.height / 2, 0f, animation.getAnimationFactor(), animation.getAnimationFactor(), 0.0) {
                    superheroFont.drawStringWithShadow(
                        text,
                        0f,
                        0f,
                        colour
                    )
                }
            }
        }
    }

}