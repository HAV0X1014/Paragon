package com.paragon.impl.module.hud.impl

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object CPS : TextHUDModule(
    "CPS",
    "Draws your current crystals per second on the screen",
    { "CPS ${TextFormatting.GRAY}[${TextFormatting.WHITE}${CPS.attackedCrystals}${TextFormatting.GRAY}]" }
) {
    private var attackedCrystals = 0.0
    private var actualACrystals = 0.0
    val timer = Timer()

    override fun onTick() {
        if (timer.hasMSPassed(1000.0)) {
            attackedCrystals = actualACrystals
            actualACrystals = 0.0
            timer.reset()
        }
    }

    @Listener
    fun onPacket(event: PacketEvent.PostSend) {
        if (event.packet is CPacketUseEntity && event.packet.action == CPacketUseEntity.Action.ATTACK && event.packet.getEntityFromWorld(mc.world) is EntityEnderCrystal) {
            actualACrystals++
        }
    }
}