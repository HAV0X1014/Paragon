package com.paragon.impl.event.network

import com.paragon.bus.event.CancellableEvent
import net.minecraft.network.Packet

/**
 * @author Surge
 */
open class PacketEvent(val packet: Packet<*>) : CancellableEvent() {

    /**
     * Fired before a packet is being processed.
     */
    class PreReceive(packet: Packet<*>) : PacketEvent(packet)

    /**
     * Fired before a packet is sent.
     */
    class PreSend(packet: Packet<*>) : PacketEvent(packet)

    /**
     * Fired after a packet has been processed.
     */
    class PostReceive(packet: Packet<*>) : PacketEvent(packet)

    /**
     * Fired after a packet has been sent.
     */
    class PostSend(packet: Packet<*>) : PacketEvent(packet)

}