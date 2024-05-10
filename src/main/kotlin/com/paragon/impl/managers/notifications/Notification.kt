package com.paragon.impl.managers.notifications

import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

/**
 * @author Surge
 */
class Notification(val message: String, val type: NotificationType) {

    val animation: Animation = Animation({ 500f }, false, { Easing.BACK_IN_OUT })
    val progress: Animation = Animation({ 800f }, false, { Easing.LINEAR })

    fun render(y: Float) {
        animation.state = if (!progress.state) {
            true
        } else {
            progress.getAnimationFactor() != 1.0
        }

        if (animation.getAnimationFactor() == 1.0) {
            progress.state = true
        }

        val scaledResolution = ScaledResolution(mc)

        val width = (FontUtil.getStringWidth(message) + 12f).coerceAtLeast(75f)
        val x = scaledResolution.scaledWidth - ((width + 10) * animation.getAnimationFactor()).toFloat()

        RenderUtil.drawRect(x, y, width, 20f, Color(0, 0, 0, 150))

        FontUtil.drawStringWithShadow(message, x + 6, y + 6, Color.WHITE)
        RenderUtil.drawRect(x, y + 19f, width * progress.getAnimationFactor().toFloat(), 1f, type.colour)
    }

}