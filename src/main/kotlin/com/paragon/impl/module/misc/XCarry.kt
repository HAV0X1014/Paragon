package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.module.Module
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.ICPacketCloseWindow
import com.paragon.util.mc
import net.minecraft.network.play.client.CPacketCloseWindow

/**
 * @author Surge
 */
object XCarry : Module("XCarry", Category.MISC, "Lets you carry items in your crafting grid") {

    @Listener
    fun onPacketSent(event: PreSend) {
        if (event.packet is CPacketCloseWindow && (event.packet as ICPacketCloseWindow).hookGetWindowId() == mc.player.inventoryContainer.windowId) {
            event.cancel()
        }
    }

}