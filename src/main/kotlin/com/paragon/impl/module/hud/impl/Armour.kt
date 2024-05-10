package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.world.BlockUtil.getBlockAtPos
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11.*

/**
 * @author Surge
 * @since 25/12/2022
 */
object Armour : HUDModule("Armour", "Draws your armour to the screen", { 72f }, { 22f }) {

    private val waterOffset = Setting("WaterOffset", true) describedBy "Position higher when you are underwater"
    private val durability = Setting("Durability", true) describedBy "Draws the armour pieces' durability"

    override fun draw() {
        val armourList: ArrayList<ItemStack> = ArrayList(mc.player.inventory.armorInventory)

        armourList.reverse()

        glPushMatrix()

        if (waterOffset.value && mc.player.position.up().getBlockAtPos() == Blocks.WATER) {
            glTranslated(0.0, -10.0, 0.0)
        }

        armourList.forEachIndexed { index, stack ->
            val stackX = x + (18 * index)

            RenderUtil.drawItemStack(stack, stackX + 1.5f, y + 6, true)

            if (durability.value) {
                val itemDamage = ((1 - stack.itemDamage.toFloat() / stack.maxDamage.toFloat()) * 100).toInt()

                RenderUtil.scaleTo(stackX + 9, y + 2, 0f, 0.8, 0.8, 1.0) {
                    FontUtil.drawCenteredString(itemDamage.toString(), stackX + 9, y + 2, Colours.mainColour.value)
                }
            }
        }

        glPopMatrix()
    }

}