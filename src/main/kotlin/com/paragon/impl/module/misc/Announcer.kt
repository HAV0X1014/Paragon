package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PlayerEvent.PlayerJoinEvent
import com.paragon.impl.event.network.PlayerEvent.PlayerLeaveEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import net.minecraftforge.event.world.BlockEvent.BreakEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge
 */
object Announcer : Module("Announcer", Category.MISC, "Announces events to the chat") {

    // Event settings
    private val chatTimer = Setting(
        "Delay", 5.0, 1.0, 60.0, 1.0
    ) describedBy "The amount of time in seconds between each chat message"

    private val breakBlocks = Setting(
        "BreakBlocks", true
    ) describedBy "Announce when a block is broken"

    private val playerJoin = Setting(
        "PlayerJoin", true
    ) describedBy "Announce when players join the server"

    private val playerLeave = Setting(
        "PlayerLeave", true
    ) describedBy "Announce when players leave the server"

    // Timer to determine when we should send the message
    private val timer = Timer()

    // Part 1 is the first part of the message, Part 2 is the value, Part 3 is the second part of the message
    private var announceComponents = arrayOf("", "0", "")

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        if (timer.hasMSPassed(chatTimer.value * 1000) && announceComponents[0] != "" && announceComponents[2] != "") {
            mc.player.sendChatMessage(announceComponents[0] + announceComponents[1] + announceComponents[2])
            announceComponents = arrayOf("", "0", "")
            timer.reset()
        }
    }

    @SubscribeEvent
    fun onBlockBreak(event: BreakEvent?) {
        if (breakBlocks.value) {
            val first = "I just broke "
            val third = " blocks!"
            if (announceComponents[0] != first && announceComponents[2] != third) {
                announceComponents = arrayOf(first, "0", third)
            }
            announceComponents = arrayOf(first, (announceComponents[1].toInt() + 1).toString(), third)
        }
    }

    @Listener
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (playerJoin.value && event.name != mc.player.name) {
            mc.player.sendChatMessage("Welcome to the server, " + event.name + "!")
        }
    }

    @Listener
    fun onPlayerLeave(event: PlayerLeaveEvent) {
        if (playerLeave.value && event.name != mc.player.name) {
            mc.player.sendChatMessage("See you later, " + event.name + "!")
        }
    }

}