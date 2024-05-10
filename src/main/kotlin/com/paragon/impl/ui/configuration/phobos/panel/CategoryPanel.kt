package com.paragon.impl.ui.configuration.phobos.panel

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.client.Colours
import com.paragon.impl.ui.configuration.phobos.element.ModuleElement
import com.paragon.impl.ui.configuration.shared.Panel
import com.paragon.impl.ui.util.Click
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.string.StringUtil
import java.awt.Color

/**
 * @author Surge
 * @since 31/07/2022
 */
class CategoryPanel(val category: Category, x: Float, y: Float, width: Float, height: Float) : Panel(x, y, width, height) {

    val modules: ArrayList<ModuleElement> = ArrayList()

    private var expanded = true
    var totalHeight = 0f

    init {
        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.category == category }.forEach {
            modules.add(ModuleElement(it, this, x + 2, y, width - 4, height))
        }
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        super.draw(mouseX, mouseY, mouseDelta)

        RenderUtil.drawRect(x, y, width, height, Colours.mainColour.value)
        FontUtil.drawStringWithShadow(StringUtil.getFormattedText(category), x + 5, y + 3f, Color.WHITE)

        totalHeight = height

        if (expanded) {
            var moduleHeight = 3f

            modules.forEach {
                moduleHeight += it.getAbsoluteHeight() + 1f
            }

            RenderUtil.drawRect(x, y + height, width, moduleHeight, Color(0, 0, 0, 180))

            var y = y + height + 2f

            modules.forEach {
                it.x = x + 2
                it.y = y

                it.draw(mouseX, mouseY, mouseDelta)

                y += it.getAbsoluteHeight() + 1f
            }

            totalHeight += moduleHeight
        }
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseClicked(mouseX, mouseY, click)

        if (isHovered(mouseX, mouseY) && click == Click.RIGHT) {
            expanded = !expanded
        }

        if (expanded) {
            modules.forEach { it.mouseClicked(mouseX, mouseY, click) }
        }
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, click: Click) {
        super.mouseReleased(mouseX, mouseY, click)

        if (expanded) {
            modules.forEach { it.mouseReleased(mouseX, mouseY, click) }
        }
    }

    override fun keyTyped(character: Char, keyCode: Int) {
        super.keyTyped(character, keyCode)

        if (expanded) {
            modules.forEach { it.keyTyped(character, keyCode) }
        }
    }

}