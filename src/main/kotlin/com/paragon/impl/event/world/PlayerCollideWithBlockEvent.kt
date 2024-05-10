package com.paragon.impl.event.world

import com.paragon.bus.event.CancellableEvent
import net.minecraft.block.Block
import net.minecraft.util.math.BlockPos

/**
 * @author SooStrator1136
 */
class PlayerCollideWithBlockEvent(val pos: BlockPos, val blockType: Block) : CancellableEvent()