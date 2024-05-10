package com.paragon.impl.event.world.entity

import com.paragon.bus.event.CancellableEvent
import net.minecraft.entity.Entity

/**
 * @author Surge, SooStrator1136
 */
class EntityPushEvent(val entity: Entity) : CancellableEvent()