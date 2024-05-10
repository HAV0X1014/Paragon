package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.event.network.ServerEvent
import net.minecraft.network.play.server.SPacketTimeUpdate
import java.util.*

class LagCompensator {

    private val tickRates = FloatArray(100)
    private var index = 0
    private var timeLastTimeUpdate: Long = 0

    val tickRate: Float
        get() {
            var numTicks = 0.0f
            var sumTickRates = 0.0f
            tickRates.forEach {
                if (it > 0.0F) {
                    sumTickRates += it
                    numTicks += 1.0f
                }
            }
            val calcTickRate = (sumTickRates / numTicks).coerceIn(0.0F..20.0F)
            return if (calcTickRate == 0.0f) 20.0F else calcTickRate
        }

    val adjustTicks: Float
        get() = tickRate - 20.0F
    val syncTicks: Float
        get() = 20.0F - tickRate
    val factor: Float
        get() = 20.0F / tickRate

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        if (event.packet !is SPacketTimeUpdate) {
            return
        }

        if (timeLastTimeUpdate != -1L) {
            val timeElapsed = (System.currentTimeMillis() - timeLastTimeUpdate).toFloat() / 1000.0F
            tickRates[index] = (20.0F / timeElapsed).coerceIn(0.0F..20.0F)
            index = (index + 1) % tickRates.size
        }

        timeLastTimeUpdate = System.currentTimeMillis()
    }

    @Listener
    fun onConnect(event: ServerEvent.Connect) = reset()

    private fun reset() {
        index = 0
        timeLastTimeUpdate = -1L
        Arrays.fill(tickRates, 0.0F)
    }

    init {
        Paragon.INSTANCE.eventBus.register(this)
    }

}