package com.paragon.impl.event.input

import net.minecraft.client.settings.KeyBinding
import com.paragon.bus.event.CancellableEvent

/**
 * Fired when a [KeyBinding] is pressed.
 */
class KeybindingPressedEvent(val keyCode: Int, var pressedState: Boolean) : CancellableEvent()