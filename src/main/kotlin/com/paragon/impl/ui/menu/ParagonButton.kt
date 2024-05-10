package com.paragon.impl.ui.menu

import com.paragon.impl.module.client.Colours
import com.paragon.util.render.ColourUtil.toColour
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color

/**
 * @author Surge
 */
class ParagonButton(buttonId: Int, x: Int, y: Int, widthIn: Int, heightIn: Int, buttonText: String) : GuiButton(buttonId, x, y, widthIn, heightIn, buttonText) {

    private val animation = Animation({ 300.0f }, false, { Easing.EXPO_IN_OUT })

    override fun drawButton(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (visible) {
            hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height
            animation.state = hovered

            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            )
            GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            )

            RenderUtil.drawRect(
                x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), if (hovered) 0x70000000.toColour() else 0x80000000.toInt().toColour()
            )

            RenderUtil.drawRect(
                x + (width / 2f - width / 2f * animation.getAnimationFactor().toFloat()), (y + height - 1).toFloat(), (width * animation.getAnimationFactor()).toFloat(), 1f, Colours.mainColour.value
            )

            mouseDragged(mc, mouseX, mouseY)

            FontUtil.drawCenteredString(displayString, x + width / 2f, y + (height / 2f) + 1.5f, Color.WHITE)
        }
    }

}