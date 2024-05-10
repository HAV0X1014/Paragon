package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PostSend
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.ICPacketPlayer
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayer

/**
 * @author GentlemanMC
 * @since the 10th of August 2022
 */
object AntiHunger : Module("AntiHunger", Category.MISC, "Tries to remove huger lost") {

    private val groundSpoof = Setting(
        "GroundSpoof", true
    ) describedBy "Spoofs your onGround state"
    private val antiSprint = Setting(
        "AntiSprint", true
    ) describedBy "Kassuk tries to code"

    private var previousSprint = false

    override fun onEnable() {
        if (mc.anyNull) {
            return
        }

        if (mc.player.isSprinting) {
            previousSprint = true
            mc.player.connection.sendPacket(
                CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING)
            )
        }
    }

    override fun onDisable() {
        if (mc.anyNull) {
            return
        }

        if (previousSprint) {
            previousSprint = false
            mc.player.connection.sendPacket(
                CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING)
            )
        }
    }

    @Listener
    fun onPacketSend(event: PostSend) {
        if (event.packet is CPacketPlayer) {
            if (groundSpoof.value && !mc.player.isRiding && !mc.player.isElytraFlying) {
                (event.packet as ICPacketPlayer).hookSetOnGround(true)
            }
        }
        else if (event.packet is CPacketEntityAction) { //Kassuk part
            val packet = event.packet
            if (packet.action == CPacketEntityAction.Action.START_SPRINTING || packet.action == CPacketEntityAction.Action.STOP_SPRINTING) {
                if (antiSprint.value) {
                    event.isCancelled()
                }
            }
        }
    }

}