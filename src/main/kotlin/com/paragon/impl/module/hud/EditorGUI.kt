package com.paragon.impl.module.hud

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.panel.impl.CategoryPanel
import com.paragon.impl.ui.util.Click
import com.paragon.util.isHovered
import com.paragon.util.render.ColourUtil.fade
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.toBinary
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.GL_BLEND
import org.lwjgl.opengl.GL11.glEnable
import java.awt.Color

/**
 * @author Surge
 * @since 24/12/2022
 */
class EditorGUI : GuiScreen() {

    private var dragging = false
    private val panel = CategoryPanel(null, Category.HUD, 200f, 20f, 80f, 22f, 200.0)
    private val tips = Animation(400f, true, Easing.BACK_IN_OUT)

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        glEnable(GL_BLEND)

        if (ClickGUI.darkenBackground.value) {
            drawDefaultBackground()
        }

        RenderUtil.drawRect(width / 2f, 0f, 0.5f, height.toFloat(), Color.WHITE)
        RenderUtil.drawRect(0f, height / 2f - 0.25f, width.toFloat(), 0.5f, Color.WHITE)

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            it as HUDModule

            it.updatePosition(mouseX, mouseY)

            RenderUtil.drawRect(it.x, it.y, it.width.invoke(), it.height.invoke(), Color(0, 0, 0, 150).fade(Color(20, 20, 20, 150), isHovered(it.x, it.y, it.width.invoke(), it.height.invoke(), mouseX, mouseY).toBinary().toDouble()))
            RenderUtil.drawBorder(it.x, it.y, it.width.invoke(), it.height.invoke(), 0.5f, Color(255, 255, 255, 200))

            it.dummy()
        }

        tips.state = !isHovered(0f, height - 25f, FontUtil.getStringWidth("Hold Left Click to drag"), 25f, mouseX, mouseY)
        FontUtil.drawString("Hold Left Click to drag${System.lineSeparator()}Right Click to disable", 5f, (height + 5) - 30f * tips.getAnimationFactor().toFloat(), Color.WHITE)

        panel.draw(mouseX.toFloat(), mouseY.toFloat(), Mouse.getDWheel())
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        run {
            Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.reversed().forEach {
                if (!dragging) {
                    if ((it as HUDModule).mouseClicked(mouseX, mouseY, mouseButton)) {
                        return@run
                    }

                    if (it.dragging) {
                        dragging = true
                    }
                }
            }
        }

        if (!dragging) {
            panel.mouseClicked(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(mouseButton))
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).dragging = false
        }

        dragging = false

        panel.mouseReleased(mouseX.toFloat(), mouseY.toFloat(), Click.getClick(state))

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        dragging = false

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it is HUDModule && it.isEnabled }.forEach {
            (it as HUDModule).dragging = false
        }
    }

}