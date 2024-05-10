package com.paragon.impl.module.render

import com.paragon.impl.event.render.gui.RenderChatEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import java.awt.Color

/**
 * @author Surge
 * @since 19/06/22
 */
object ClearChat : Module("ClearChat", Category.RENDER, "Removes the chat background") {

    private val colour = Setting(
        "Colour", Color(0, 0, 0, 0)
    ) describedBy "The colour of the chat background"

    @Listener
    fun onRenderChatBackground(event: RenderChatEvent) {
        event.colour = colour.value.rgb
    }

}