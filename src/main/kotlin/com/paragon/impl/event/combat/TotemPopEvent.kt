package com.paragon.impl.event.combat

import com.paragon.bus.event.Event
import net.minecraft.entity.player.EntityPlayer

/**
 * Fired when a player pops.
 *
 * @author Surge
 */
class TotemPopEvent(val player: EntityPlayer) : Event()