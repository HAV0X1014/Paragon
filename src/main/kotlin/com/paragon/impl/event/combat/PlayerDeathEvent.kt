package com.paragon.impl.event.combat

import com.paragon.bus.event.Event
import net.minecraft.entity.player.EntityPlayer

/**
 * Fired when a player dies.
 *
 * @author Surge
 */
class PlayerDeathEvent(val player: EntityPlayer, val pops: Int) : Event()