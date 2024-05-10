package com.paragon.impl.event.render.world

import com.paragon.bus.event.CancellableEvent
import net.minecraft.util.math.BlockPos

/**
 * @author Surge
 */
class RenderBlockModelEvent(val pos: BlockPos) : CancellableEvent() {

    var returnValue = false

}