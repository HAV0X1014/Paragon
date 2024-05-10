package com.paragon.impl.event.render.gui

import com.paragon.bus.event.CancellableEvent
import net.minecraft.client.gui.GuiScreen

data class GuiUpdateEvent(var screen: GuiScreen?) : CancellableEvent()