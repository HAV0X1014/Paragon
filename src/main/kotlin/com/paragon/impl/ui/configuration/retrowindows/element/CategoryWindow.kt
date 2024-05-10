package com.paragon.impl.ui.configuration.retrowindows.element

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.retrowindows.element.module.ModuleElement
import com.paragon.impl.ui.configuration.shared.Panel
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.string.StringUtil
import me.surge.animation.Animation
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * @author Surge
 */
class CategoryWindow(category: Category, x: Float, y: Float, width: Float, height: Float) : Panel(x, y, width, height) {

    private val moduleElements = ArrayList<ModuleElement>()
    val animation = Animation(ClickGUI.animationSpeed::value, true, ClickGUI.easing::value)
    var scissorHeight = 0f

    var tooltipName = ""
    var tooltipContent = ""

    // Could make changeable, but I don't think it's necessary.
    private val maxHeight = 320f

    init {
        title = StringUtil.getFormattedText(category)

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.category == category }.forEach {
            moduleElements.add(ModuleElement(this, it, x, y + height, width - 1, height))
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        // Reset tooltip
        tooltipName = ""
        tooltipContent = ""

        var moduleHeight = 0f

        moduleElements.forEach {
            moduleHeight += it.getTotalHeight()
        }

        scissorHeight = (MathHelper.clamp(moduleHeight, 0f, maxHeight) * animation.getAnimationFactor()).toFloat()

        // Background
        RenderUtil.drawRect(x, y, width, (height + scissorHeight + 1 * animation.getAnimationFactor()).toFloat(), Color(148, 148, 148))
        RenderUtil.drawHorizontalGradientRect(x + 1, y + 1, width - 2, height - 2, Colours.mainColour.value, if (ClickGUI.gradient.value) Colours.mainColour.value.brighter().brighter() else Colours.mainColour.value)

        // Minimise button
        RenderUtil.drawRect(x + width - 12.5f, y + 3.5f, 10f, 10f, Color(70, 70, 70))
        RenderUtil.drawRect(x + width - 13.5f, y + 2.5f, 10f, 10f, Color(148, 148, 148))

        RenderUtil.drawRect(x + width - 11.5f, y + 7f, 6f, 1f, Color(50, 50, 50))

        if (!animation.state) {
            RenderUtil.drawRect(x + width - 9f, y + 4.5f, 1f, 6f, Color(50, 50, 50))
        }

        FontUtil.drawStringWithShadow(title, x + 5, y + 4, Color.WHITE)

        if (animation.getAnimationFactor() > 0) {
            if (mouseX in x..x + width && mouseY in y + height..y + height + scissorHeight) {
                if (mouseDelta < 0 && moduleElements[moduleElements.size - 1].y + moduleElements[moduleElements.size - 1].getTotalHeight() > y + height + scissorHeight) {
                    moduleElements.forEach {
                        it.y -= height
                    }
                }
                else if (mouseDelta > 0 && moduleElements[0].y < y + height) {
                    moduleElements.forEach {
                        it.y += height
                    }
                }
            }

            if (moduleElements[moduleElements.size - 1].y + moduleElements[moduleElements.size - 1].getTotalHeight() < y + height + scissorHeight) {
                var new = moduleElements[0].y + 4

                if (new > y + height) {
                    new = moduleElements[0].y + 1
                }

                moduleElements[0].y = new
            }

            if (moduleElements[0].y > y + height) {
                moduleElements[0].y = y + height
            }

            RenderUtil.pushScissor(x, (y + height), width, scissorHeight)

            var yOffset = moduleElements[0].y
            moduleElements.forEach {
                it.x = x
                it.y = yOffset

                it.draw(mouseX, mouseY, mouseDelta)

                yOffset += it.getTotalHeight()
            }

            RenderUtil.popScissor()
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        moduleElements.forEach { it.mouseClicked(mouseX, mouseY, click) }

        if (isHovered(mouseX, mouseY)) {
            if (click == Click.RIGHT) {
                animation.state = !animation.state
            }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        moduleElements.forEach { it.mouseReleased(mouseX, mouseY, click) }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        moduleElements.forEach { it.keyTyped(character, keyCode) }
    }

}