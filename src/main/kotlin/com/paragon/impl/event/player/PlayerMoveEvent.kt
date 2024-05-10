package com.paragon.impl.event.player

import com.paragon.bus.event.Event

/**
 * Fired before a player is moved to a new position.
 *
 * @author Surge
 */
class PlayerMoveEvent(var x: Double, var y: Double, var z: Double) : Event()