package com.paragon.impl.module.combat

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.event.player.PlayerMoveEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.mixins.accessor.ICPacketPlayer
import com.paragon.mixins.accessor.INetworkManager
import com.paragon.util.mc
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity

/**
 * @author Surge, aesthetical
 */
object Criticals : Module("Criticals", Category.COMBAT, "Makes all your hits critical hits") {

    val mode = Setting("Mode", Mode.PACKET) describedBy "How to enforce a critical attack"

    private var pauseTicks = 0

    override fun onDisable() {
        pauseTicks = 0
    }

    @Listener
    fun onPacketSend(event: PacketEvent.PreSend) {
        // We are attacking an entity
        if (event.packet is CPacketUseEntity) {

            // Check the packets action and if the entity we are attacking is a living entity
            if (event.packet.action != CPacketUseEntity.Action.ATTACK || event.packet.getEntityFromWorld(mc.world) !is EntityLivingBase) {
                return
            }

            // We are on the ground and we aren't jumping
            if (mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown) {

                // Send packets
                when (mode.value) {
                    Mode.PACKET -> {
                        send(0.1)
                        send(0.0)
                    }

                    Mode.UPDATED_NCP -> {
                        pauseTicks = 2

                        send(0.11)
                        send(0.1100013579)
                        send(0.0000013579)
                    }

                    Mode.MINIS -> mc.player.motionY = 0.2
                }
            }
        } else if (event.packet is CPacketPlayer) {

            if ((event.packet as ICPacketPlayer).hookIsMoving() && pauseTicks > 0) {
                event.cancel()
            }
        }
    }

    @Listener
    fun onMotion(event: PlayerMoveEvent) {
        if (pauseTicks-- > 0) {
            event.x = 0.0
            event.z = 0.0

            mc.player.motionX = 0.0
            mc.player.motionZ = 0.0
        }
    }

    private fun send(yOffset: Double) {
        (mc.player.connection.networkManager as INetworkManager).hookDispatchPacket(CPacketPlayer.Position(
            mc.player.posX, mc.player.posY + yOffset, mc.player.posZ, false
        ), null)
    }

    enum class Mode {
        /**
         * Normal packet criticals. Should at most bypass old NCP
         */
        PACKET,

        /**
         * Bypasses updated NoCheatPlus, downside is it sends more packets
         */
        UPDATED_NCP,

        /**
         * Sets your motionY to .2
         */
        MINIS
    }
}