package com.paragon.impl.module.combat

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.Enchantments
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.util.EnumHand

/**
 * @author Surge
 * @since 03/12/2022
 */
object AutoXP : Module("AutoXP", Category.COMBAT, "Automatically repairs armour") {

    private val threshold = Setting("Threshold", 20.0, 1.0, 60.0, 1.0) describedBy "The percentage of the armour's durability to start repairing at"

    // The piece we are currently repairing
    private var repairing: ItemStack? = null

    override fun onTick() {

        // Being in creative can cause us to spam experience bottles with no end causing EXTREME lag.
        if (mc.anyNull || mc.player.isCreative) {

            // Reset repairing
            repairing = null
            return
        }

        // The lowest durability of an item
        var lowest = Float.MAX_VALUE

        // Iterate through armour slots
        for (armour in mc.player.armorInventoryList) {

            // Don't do anything if there isn't an item in the slot
            if (armour.isEmpty || !EnchantmentHelper.getEnchantments(armour).containsKey(Enchantments.MENDING) || armour.maxDamage == 0) {
                continue
            }

            // Damage of an item
            val damage = (1 - armour.itemDamage.toFloat() / armour.maxDamage.toFloat()) * 100

            if (damage <= threshold.value && damage < lowest) {

                // Set repairing piece
                repairing = armour
                lowest = damage
            }
        }

        // Not lower than the threshold
        if (lowest > threshold.value) {
            repairing = null
            return
        }

        // Get hotbar slots
        val previousSlot = mc.player.inventory.currentItem
        val slot = InventoryUtil.getItemInHotbar(Items.EXPERIENCE_BOTTLE)

        // We haven't found a slot
        if (slot == -1) {
            return
        }

        // Rotate straight downwards
        if (Paragon.INSTANCE.rotationManager.serverRotation.y != 90f) {
            mc.player.connection.sendPacket(CPacketPlayer.Rotation(mc.player.rotationYaw, 90f, mc.player.onGround))
        }

        // Switch to experience bottle
        InventoryUtil.switchToSlot(slot, false)

        // Throw experience bottle
        mc.player.connection.sendPacket(CPacketPlayerTryUseItem(EnumHand.MAIN_HAND))

        // Switch back
        InventoryUtil.switchToSlot(previousSlot, false)
    }

    override fun getData(): String {
        return if (repairing == null) "" else "${repairing!!.displayName}, ${repairing!!.itemDamage}"
    }

}