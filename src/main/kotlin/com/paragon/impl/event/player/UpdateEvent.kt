package com.paragon.impl.event.player

import com.paragon.bus.event.CancellableEvent

/**
 * @author Surge
 */
class UpdateEvent : CancellableEvent() {
    var x = 0.0
    var y = 0.0
    var z = 0.0
    var yaw = 0f
    var pitch = 0f
    var isOnGround = false
}