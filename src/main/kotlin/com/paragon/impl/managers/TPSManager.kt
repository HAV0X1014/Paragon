package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.event.network.ServerEvent
import com.paragon.util.system.CircularArray
import net.minecraft.network.play.server.SPacketTimeUpdate

class TPSManager {

    init {
        Paragon.INSTANCE.eventBus.register(this)
    }

    // Circular Buffer lasting ~60 seconds for tick storage
    private val tickRates = CircularArray.create(120, 20f)
    private var timeLastTimeUpdate: Long = 0

    val averageTick = tickRates.average()

    val tickRate: Float
        get() = tickRates.average().coerceIn(0.0f, 20.0f)

    val adjustTicks: Float get() = tickRates.average() - 20f
    val syncTicks: Float get() = 20.0F - tickRate
    val factor: Float get() = 20.0F / tickRate

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        if (event.packet !is SPacketTimeUpdate) {
            return
        }

        if (timeLastTimeUpdate != -1L) {
            val timeElapsed = (System.nanoTime() - timeLastTimeUpdate) / 1E9

            tickRates.add((20.0 / timeElapsed).coerceIn(0.0, 20.0).toFloat())
        }

        timeLastTimeUpdate = System.nanoTime()
    }

    @Listener
    fun onConnect(event: ServerEvent.Connect) = reset()

    private fun reset() {
        tickRates.reset()
        timeLastTimeUpdate = -1L
    }

}