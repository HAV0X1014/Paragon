package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.impl.event.combat.PlayerDeathEvent
import com.paragon.impl.event.combat.TotemPopEvent
import com.paragon.impl.event.network.PacketEvent.PreReceive
import com.paragon.impl.event.world.entity.EntityRemoveFromWorldEvent
import com.paragon.bus.listener.Listener
import com.paragon.util.mc
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketEntityStatus

/**
 * @author Surge
 */
class PopManager {

    private val pops: MutableMap<EntityPlayer, Int> = HashMap()

    init {
        Paragon.INSTANCE.eventBus.register(this)
    }

    @Listener
    fun onPacketReceive(event: PreReceive) {
        if (event.packet is SPacketEntityStatus && event.packet.opCode.toInt() == 35 && (event.packet as SPacketEntityStatus).getEntity(mc.world) is EntityPlayer) {
            val packet = event.packet

            pops[packet.getEntity(mc.world) as EntityPlayer] = if (pops.containsKey(packet.getEntity(mc.world) as EntityPlayer)) pops[packet.getEntity(mc.world) as EntityPlayer]!! + 1 else 1

            Paragon.INSTANCE.eventBus.post(TotemPopEvent((event.packet.getEntity(mc.world) as EntityPlayer)))
        }
    }

    @Listener
    fun onEntityRemove(event: EntityRemoveFromWorldEvent) {
        if (event.entity is EntityPlayer && event.entity != mc.player) {
            if (pops.containsKey(event.entity)) {
                val playerDeathEvent = PlayerDeathEvent(event.entity, getPops(event.entity))
                Paragon.INSTANCE.eventBus.post(playerDeathEvent)

                pops.remove(event.entity)
            }
        }
    }

    fun getPops(player: EntityPlayer) = pops.getOrDefault(player, 0)

}