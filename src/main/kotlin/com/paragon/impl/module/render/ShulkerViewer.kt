package com.paragon.impl.module.render

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.gui.RenderTooltipEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.client.Colours
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemShulkerBox
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import java.awt.Color
import kotlin.math.max

/**
 * @author Surge
 */
object ShulkerViewer : Module("ShulkerViewer", Category.RENDER, "Preview shulker contents") {

    @Listener
    fun onRenderTooltip(event: RenderTooltipEvent) {
        if (event.stack.item !is ItemShulkerBox) {
            return
        }

        // Get stack compound
        val compound = event.stack.tagCompound

        // Has items
        if (compound != null && compound.hasKey("BlockEntityTag") && compound.getCompoundTag("BlockEntityTag").hasKey("Items", 9)) {
            event.cancel()

            // Translate so the tooltip is above other items etc
            GlStateManager.translate(0f, 0f, 500f)

            // Get item list in shulker
            val items = NonNullList.withSize(27, ItemStack.EMPTY)
            ItemStackHelper.loadAllItems(compound.getCompoundTag("BlockEntityTag"), items)

            // Y offset
            val y = event.y - 31

            val width = max(166f, FontUtil.getStringWidth(event.stack.displayName) + 12)

            // Background
            RenderUtil.drawRect(event.x + 2, y, width, 71f, Color(23, 23, 25))

            // Border
            RenderUtil.drawBorder(event.x + 2, y, width, 71f, 1f, Colours.mainColour.value)

            // Shulker box name
            mc.fontRenderer.drawStringWithShadow(event.stack.displayName, event.x + 6, y + 3.5f, -1)

            // Item X and Y
            var itemX = event.x + 5
            var itemY = y + 16

            // Count of thing to determine when to start a new row
            var a = 0

            // Iterate through items
            for (item in items) {
                // Background thing
                RenderUtil.drawRect(itemX - 0.5f, itemY - 0.5f, 17f, 17f, Color(35, 35, 38))

                // Render stack
                RenderUtil.drawItemStack(item, itemX, itemY, true)

                // Increase count
                a++

                // Increase X
                itemX += 18f

                // If count is not 0, and is divisible by 9, move to the next row
                if (a != 0 && a % 9 == 0) {
                    itemX = event.x + 5
                    itemY += 18f
                }
            }
        }
    }

}