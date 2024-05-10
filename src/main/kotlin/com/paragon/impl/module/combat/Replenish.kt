package com.paragon.impl.module.combat

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.inventory.ClickType
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketCloseWindow
import net.minecraft.network.play.client.CPacketEntityAction

/**
 * @author Surge
 */
@Aliases(["HotbarRefill", "Refill"])
object Replenish : Module("Replenish", Category.COMBAT, "Automatically refills items in your hotbar") {

    private val inventorySpoof = Setting(
        "InventorySpoof", true
    ) describedBy "Spoofs opening your inventory"

    private val percent = Setting(
        "Percent", 50f, 1f, 100f, 1f
    ) describedBy "The point at which to refill"

    override fun onTick() {
        if (mc.anyNull || mc.player.ticksExisted < 20 || mc.player.isDead) {
            return
        }

        // Loop through hotbar items
        for (i in 0..8) {
            // Get the stack
            val stack = mc.player.inventory.getStackInSlot(i)

            // If the stack is empty, continue
            if (stack.isEmpty) {
                continue
            }

            // Get percentage of item in the slot
            val stackPercent = stack.count.toDouble() / stack.maxStackSize.toDouble() * 100.0

            // Check if the item is below the threshold
            if (stackPercent <= percent.value.toInt()) {
                mergeStack(i, stack)

                // Stop merging - 1 per tick
                break
            }
        }
    }

    private fun mergeStack(current: Int, stack: ItemStack) {
        var replaceSlot = -1

        // Loop through items in inventory
        for (i in 9..35) {
            // Get the stack
            val inventoryStack = mc.player.inventory.getStackInSlot(i)

            // If the stack is empty, continue
            if (inventoryStack.isEmpty) {
                continue
            }

            // The name needs to be the same as the current stack's name, otherwise they can't be merged
            if (inventoryStack.displayName != stack.displayName) {
                continue
            }

            // We want to merge blocks
            if (stack.item is ItemBlock && inventoryStack.item is ItemBlock) {
                // Check the blocks are the same
                if ((stack.item as ItemBlock).block != (inventoryStack.item as ItemBlock).block) {
                    continue
                }
            }
            else {
                // Check the items are the same
                if (stack.item != inventoryStack.item) {
                    continue
                }
            }

            // Set replace slot
            replaceSlot = i
            break
        }
        if (replaceSlot != -1) {
            if (inventorySpoof.value) {
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player, CPacketEntityAction.Action.OPEN_INVENTORY
                    )
                )
            }

            // Merge stacks
            mc.playerController.windowClick(0, replaceSlot, 0, ClickType.PICKUP, mc.player)
            mc.playerController.windowClick(0, if (current < 9) current + 36 else current, 0, ClickType.PICKUP, mc.player)
            mc.playerController.windowClick(0, replaceSlot, 0, ClickType.PICKUP, mc.player)

            if (inventorySpoof.value) {
                mc.player.connection.sendPacket(CPacketCloseWindow(mc.player.inventoryContainer.windowId))
            }
        }
    }

}