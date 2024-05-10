package com.paragon.impl.module.hud.impl

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.hud.TextHUDModule
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object SentPackets : TextHUDModule(
    "SentPackets",
    "Draws the amount of packets you have sent in the last tick to the screen",
    { "SentPackets ${TextFormatting.GRAY}[${TextFormatting.WHITE}${SentPackets.lastCount}${TextFormatting.GRAY}]" }
) {
    private var currentCount = 0
    private var lastCount = 0

    override fun onTick() {
        lastCount = currentCount
        currentCount = 0
    }

    @Listener
    fun onPacketSend(event: PacketEvent.PostSend) {
        currentCount++
    }
}