package com.paragon.impl.module.movement

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.player.WalkOffOfBlockEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module

/**
 * @author Surge
 * @since 03/12/2022
 */
object SafeWalk : Module("SafeWalk", Category.MOVEMENT, "Prevents you from walking off of the edges of blocks") {

    @Listener
    fun onWalkOffOfBlock(event: WalkOffOfBlockEvent) {
        event.cancel()
    }

}