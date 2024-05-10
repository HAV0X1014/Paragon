package com.paragon.impl.event.player

import com.paragon.bus.event.CancellableEvent
import net.minecraft.entity.Entity

/**
 * @author Surge
 * @since 03/12/2022
 */
class WalkOffOfBlockEvent(val entity: Entity) : CancellableEvent()