package com.paragon.util.player

import com.paragon.util.mc
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumHand

object InventoryUtil {

    fun isHolding(item: Item): Boolean {
        return mc.player.heldItemMainhand.item == item || mc.player.heldItemOffhand.item == item
    }

    /**
     * Checks whether the player is holding the given [Item] in the given [EnumHand].
     *
     * @return the result of the check.
     */
    fun isHolding(item: Item, hand: EnumHand) = mc.player.getHeldItem(hand).item == item

    /**
     * @return The hand holding the given [Item], null if the player isn't holding it.
     */
    fun getHandHolding(item: Item) = when {
        mc.player.heldItemMainhand.item === item -> EnumHand.MAIN_HAND
        mc.player.heldItemOffhand.item === item -> EnumHand.OFF_HAND
        else -> null
    }

    /**
     * @return the inventory slot the given [Item] is in.
     */
    @JvmStatic
    fun getItemSlot(itemIn: Item) = (9..35).firstOrNull { mc.player.inventory.getStackInSlot(it).item == itemIn } ?: -1

    /**
     * @return the hotbar slot of the given [Item].
     */
    @JvmStatic
    fun getItemInHotbar(itemIn: Item) = (0..8).firstOrNull { mc.player.inventory.getStackInSlot(it).item == itemIn } ?: -1

    /**
     * Switches to an item in the player's hotbar
     *
     * @param itemIn The item to switch to
     * @param packet Switch silently - use packets instead
     * @return Whether the switch was successful
     */
    fun switchToItem(itemIn: Item, packet: Boolean): Boolean {
        if (isHolding(itemIn) || getItemInHotbar(itemIn) == -1) {
            return false
        }

        if (packet) {
            mc.connection!!.sendPacket(CPacketHeldItemChange(getItemInHotbar(itemIn)))
        } else {
            mc.player.inventory.currentItem = getItemInHotbar(itemIn)
        }

        return true
    }

    @JvmStatic
    fun switchToSlot(slot: Int, packet: Boolean) {
        if (slot == mc.player.inventory.currentItem) {
            return
        }

        mc.player.connection.sendPacket(CPacketHeldItemChange(slot))

        if (!packet) {
            mc.player.inventory.currentItem = slot
        }
    }

    fun getCountOfItem(item: Item, hotbarOnly: Boolean, ignoreHotbar: Boolean): Int {
        var count = 0
        for (i in (if (ignoreHotbar) 9 else 0) until if (hotbarOnly) 9 else 36) {
            val stack = mc.player.inventory.getStackInSlot(i)
            if (stack.item === item) {
                count += stack.count
            }
        }

        return count
    }

    val isHoldingSword: Boolean
        get() = mc.player.heldItemMainhand.item is ItemSword

    /**
     * @return the hotbar slot of the given [Block].
     */
    @JvmStatic
    fun getHotbarBlockSlot(block: Block) = (0..8).firstOrNull { i ->
        mc.player.inventory.getStackInSlot(i).item.let {
            it is ItemBlock && it.block == block
        }
    } ?: -1

}