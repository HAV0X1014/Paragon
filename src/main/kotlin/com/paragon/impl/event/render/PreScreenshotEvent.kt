package com.paragon.impl.event.render

import com.paragon.bus.event.CancellableEvent
import net.minecraft.util.text.ITextComponent

/**
 * @author SooStrator1136
 */
class PreScreenshotEvent(var response: ITextComponent?) : CancellableEvent()