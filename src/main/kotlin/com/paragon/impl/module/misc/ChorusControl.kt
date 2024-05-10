package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil.drawNametagText
import net.minecraft.item.ItemChorusFruit
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerTryUseItem
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec3d
import java.awt.Color

/**
 * @author GentlemanMC, SooStrator1136
 */
object ChorusControl : Module("ChorusControl", Category.MISC, "Cancels packets to let you not teleport") {

    private val cPacketPlayer = Setting("CPacketPlayer", true) describedBy "Utilise CPacketPlayer packets"
    private val packetPlayerPosLook = Setting(
        "SPacketPlayerPosLook", true
    ) describedBy "Utilise SPacketPlayerPosLook packets"
    private val packets: MutableCollection<CPacketPlayer> = ArrayList(10)

    private val teleportPackets: MutableCollection<CPacketConfirmTeleport> = ArrayList(2)

    private val renderPos: Vec3d? = null //Idk where to set this, your turn gentle
    private var ate = false

    override fun onDisable() {
        if (mc.connection != null) {
            packets.forEach((mc.connection ?: return)::sendPacket)
            teleportPackets.forEach((mc.connection ?: return)::sendPacket)
        }

        packets.clear()
        teleportPackets.clear()
        ate = false
    }

    override fun onRender3D() {
        if (renderPos == null) {
            return
        }

        drawNametagText("Player Chorus", renderPos, Color.WHITE)
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is CPacketPlayerTryUseItem) {
            if ((if (event.packet.hand == EnumHand.OFF_HAND) mc.player.heldItemOffhand else mc.player.heldItemMainhand).item is ItemChorusFruit) {
                ate = true
            }
        }

        if (ate) {
            if (event.packet is SPacketPlayerPosLook && packetPlayerPosLook.value) {
                event.cancel()
            }

            if (event.packet is CPacketPlayer && cPacketPlayer.value) {
                packets.add(event.packet)
                event.cancel()
            }

            if (event.packet is CPacketConfirmTeleport) {
                teleportPackets.add(event.packet)
                event.cancel()
            }
        }
    }
}