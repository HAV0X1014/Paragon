package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import java.util.*

/**
 * @author Surge
 */
object FastUse : Module("FastUse", Category.MISC, "Allows you to use items quicker than you would be able to in vanilla") {

    private val xp = Setting(
        "XP", true
    ) describedBy "Fast use XP bottles"

    private val rotate = Setting(
        "Rotate", true
    ) describedBy "Rotate your player when using XP bottles" subOf xp

    private val crystals = Setting(
        "Crystals", true
    ) describedBy "Place crystals fast"

    private val randomPause = Setting(
        "RandomPause", true
    ) describedBy "Randomly pauses to try and prevent you from being kicked"

    private val randomChance = Setting(
        "Chance", 50f, 2f, 100f, 1f
    ) describedBy "The chance to pause" subOf randomPause

    private val random = Random()

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        // Check we want to set the delay timer to 0
        if (xp.value && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || crystals.value && InventoryUtil.isHolding(Items.END_CRYSTAL)) {
            if (randomPause.value && random.nextInt(randomChance.value.toInt()) == 1) {
                (mc as IMinecraft).hookSetRightClickDelayTimer(4)
                return
            }

            if (InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE)) {
                mc.player.xpCooldown = 0
            }

            (mc as IMinecraft).hookSetRightClickDelayTimer(0)
        }
    }

    @Listener
    fun onPacketSend(event: PreSend) {
        if (event.packet !is CPacketPlayerTryUseItem || !InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE) || !xp.value || !rotate.value) {
            return
        }

        // Send rotation packet. We aren't using the rotation manager as it doesn't immediately rotate the player
        mc.player.connection.sendPacket(
            CPacketPlayer.Rotation(
                mc.player.rotationYaw, 90f, mc.player.onGround
            )
        )
    }

}