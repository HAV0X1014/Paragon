package com.paragon.impl.event.world.entity

import com.paragon.bus.event.Event
import net.minecraft.entity.Entity

/**
 * @author Surge, SooStrator1136
 */
class EntityRemoveFromWorldEvent(val entity: Entity) : Event()