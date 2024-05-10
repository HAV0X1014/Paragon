package com.paragon.impl.event.render.gui

import com.paragon.bus.event.CancellableEvent
import net.minecraft.item.ItemStack

/**
 * @author Surge
 */
class RenderTooltipEvent(val stack: ItemStack, val x: Float, val y: Float) : CancellableEvent()