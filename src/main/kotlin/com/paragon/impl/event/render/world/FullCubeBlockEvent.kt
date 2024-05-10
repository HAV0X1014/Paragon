package com.paragon.impl.event.render.world

import com.paragon.bus.event.CancellableEvent
import net.minecraft.block.Block

/**
 * @author Surge
 */
class FullCubeBlockEvent(val block: Block) : CancellableEvent() {

    var returnValue = false

}