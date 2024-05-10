package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.geom.Point2D

/**
 * @author Surge
 * @since 24/12/2022
 */
object Keystrokes : HUDModule("Keystrokes", "Draws the keys you are currently pressing to the screen", { 90f }, { 89.5f }) {

    private val animationSpeed = Setting("AnimationSpeed", 300F, 100F, 2000F, 50F) describedBy "The time to fill the rect with a circle"
    private val animationEasing = Setting("Easing", Easing.LINEAR) excludes Easing.BACK_IN

    private val backgroundColor = Setting("Background", Color(0, 0, 0, 35)) describedBy "Color of the background"
    private val circleColor = Setting("Fill color", Colours.mainColour.value) describedBy "Color of the circle filling indicating the pressed keys"

    private val keys = arrayListOf(
        Key(Keyboard.KEY_W, 1, 0f, 0f, 28f, 28f),
        Key(Keyboard.KEY_A, 2, 0f, 0f, 28f, 28f),
        Key(Keyboard.KEY_S, 2, 0f, 0f, 28f, 28f),
        Key(Keyboard.KEY_D, 2, 0f, 0f, 28f, 28f),
        Key(Keyboard.KEY_SPACE, 3, 0f, 0f, 43f, 28f),
        Key(mc.gameSettings.keyBindSprint.keyCode, 3, 0f, 0f, 43f, 28f)
    )

    override fun draw() {
        var secondLayer = x + 1
        var thirdLayer = x + 1

        keys.forEach {
            when (it.layer) {
                1 -> {
                    it.x = (x + (width.invoke() / 2f)) - (it.width / 2f)
                    it.y = y + 1
                }

                2 -> {
                    it.x = secondLayer
                    it.y = y + 31

                    secondLayer += it.width + 2
                }

                3 -> {
                    it.x = thirdLayer
                    it.y = y + 61

                    thirdLayer += it.width + 2
                }
            }

            it.draw()
        }
    }

    internal class Key(val keyCode: Int, val layer: Int, var x: Float, var y: Float, var width: Float, var height: Float) {

        private val animation = Animation({ animationSpeed.value }, false, { animationEasing.value })

        fun draw() {
            animation.state = Keyboard.isKeyDown(keyCode)

            RenderUtil.pushScissor(x, y, width, height)

            RenderUtil.drawRect(x, y, width, height, backgroundColor.value)

            if (animation.getAnimationFactor() > 0.0) {
                RenderUtil.drawCircle(x + (width / 2.0), y + (height / 2.0), Point2D.distance(x + (width / 2.0), y + (height / 2.0), x.toDouble(), y.toDouble()) * animation.getAnimationFactor(), circleColor.value)
            }

            FontUtil.drawCenteredString(Keyboard.getKeyName(keyCode), x + (width / 2f), y + (height / 2f) - 4, Color.WHITE)

            RenderUtil.popScissor()
        }

    }

}