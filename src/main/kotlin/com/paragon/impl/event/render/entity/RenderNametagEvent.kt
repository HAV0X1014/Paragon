package com.paragon.impl.event.render.entity

import com.paragon.bus.event.CancellableEvent
import net.minecraft.entity.Entity

/**
 * @author Surge
 */
class RenderNametagEvent(val entity: Entity) : CancellableEvent()