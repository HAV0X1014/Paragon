package com.paragon.impl.event.render.world

import com.paragon.bus.event.CancellableEvent
import net.minecraft.util.math.BlockPos

/**
 * @author Surge
 */
class BlockSetOpaqueEvent(val pos: BlockPos) : CancellableEvent()