package com.paragon.impl.event.player

import com.paragon.bus.event.CancellableEvent
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB

/**
 * @author Surge
 */
class StepEvent(val bB: AxisAlignedBB, val entity: Entity, var height: Float) : CancellableEvent()