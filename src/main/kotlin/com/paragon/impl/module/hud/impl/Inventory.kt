package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.HUDModule
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import java.awt.Color

/**
 * @author Surge
 * @since 24/12/2022
 */
object Inventory : HUDModule("Inventory", "Draws your inventory to the screen",
    // (18 * amount of elements) + 2 (for padding)
    { 164f }, { 56f }
) {
    override fun draw() {
        RenderUtil.drawRect(x, y, width.invoke(), height.invoke(), Color(0, 0, 0, 100))
        RenderUtil.drawBorder(x, y, width.invoke(), height.invoke(), 0.5f, Color(0, 0, 0, 200))

        dummy()
    }

    override fun dummy() {
        var itemX = x + 1
        var itemY = y + 1

        for (i in 9..35) {
            val stack = mc.player.inventory.getStackInSlot(i)

            RenderUtil.drawItemStack(stack, itemX, itemY, false)

            itemX += 18f

            if (i == 17 || i == 26) {
                itemX = x + 1
                itemY += 18f
            }
        }
    }
}