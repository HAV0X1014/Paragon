package com.paragon.impl.module.hud.impl

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.calculations.Timer
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Surge
 * @since 24/12/2022
 */
object LagNotifier : TextHUDModule(
    "LagNotifier",
    "Tells you when the server is lagging",
    {
        if (LagNotifier.lastPacketTimer.hasMSPassed(LagNotifier.threshold.value)) {
            "Server has been lagging for ${SimpleDateFormat("s").format(Date(LagNotifier.lastPacketTimer.getTime()))} seconds"
        } else {
            ""
        }
    }
) {
    private val threshold = Setting("Threshold", 1000.0, 50.0, 2000.0, 10.0) describedBy "How many milliseconds have to have passed before we consider the server to be lagging"

    private val lastPacketTimer = Timer()

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        lastPacketTimer.reset()
    }
}