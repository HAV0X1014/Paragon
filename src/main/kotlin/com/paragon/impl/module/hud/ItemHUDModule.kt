package com.paragon.impl.module.hud

import com.paragon.util.player.InventoryUtil
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import java.awt.Color

/**
 * @author Surge
 * @since 24/12/2022
 */
open class ItemHUDModule(name: String, description: String, val item: Item) : HUDModule(name, description, { 18f }, { 18f }) {

    override fun draw() {
        RenderUtil.drawRect(x, y, width.invoke(), height.invoke(), Color(0, 0, 0, 150))
        RenderUtil.drawBorder(x, y, width.invoke(), height.invoke(), 0.5f, Color(0, 0, 0, 200))

        dummy()
    }

    override fun dummy() {
        val count = InventoryUtil.getCountOfItem(item, hotbarOnly = false, ignoreHotbar = false).toString()

        val trueX = x + if (item == Items.END_CRYSTAL) 1 else 0
        val trueY = y + if (item == Items.END_CRYSTAL) 1 else 0

        RenderUtil.drawItemStack(ItemStack(item), trueX, trueY, false)
        FontUtil.drawString(count, x + width.invoke() - FontUtil.getStringWidth(count) - 1, y + height.invoke() - FontUtil.getHeight(), Color.WHITE)
    }

}