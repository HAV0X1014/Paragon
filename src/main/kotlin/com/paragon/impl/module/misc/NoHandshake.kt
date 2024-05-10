package com.paragon.impl.module.misc

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.mixins.accessor.ICPacketCustomPayload
import com.paragon.util.mc
import io.netty.buffer.Unpooled
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.CPacketCustomPayload
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket

/**
 * @author Surge
 * @since 29/11/2022
 */
object NoHandshake : Module("NoHandshake", Category.MISC, "Cancel the Forge server handshake") {

    @Listener
    fun onPacketSend(event: PacketEvent.PreSend) {
        // Don't send proxy packets if we're in multiplayer
        if (event.packet is FMLProxyPacket && !mc.isSingleplayer) {
            event.cancel()
        }

        if (event.packet is CPacketCustomPayload) {
            // Type of client
            if (event.packet.channelName == "MC|Brand") {
                // Overwrite data
                (event.packet as ICPacketCustomPayload).hookSetData(PacketBuffer(Unpooled.buffer()).writeString("vanilla"))
            }
        }
    }

}