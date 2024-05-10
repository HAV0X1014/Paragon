package com.paragon.impl.event.client

import com.paragon.impl.module.Module
import com.paragon.bus.event.Event

/**
 * Fired when a module is toggled.
 *
 * @author Surge
 */
class ModuleToggleEvent(val module: Module) : Event()