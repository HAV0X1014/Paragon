package com.paragon.impl.module.misc

import com.paragon.impl.event.render.gui.TabListEvent
import com.paragon.impl.event.render.gui.TabOverlayEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category

object ExtraTab : Module("ExtraTab", Category.MISC, "Extends the limit of players on the tab list") {

    private val limit = Setting(
        "Limit", 500f, 1f, 500f, 1f
    ) describedBy "The limit of players"

    @Listener
    fun onTabList(event: TabListEvent) {
        event.size = limit.value
        event.cancel()
    }

    @Listener
    fun onTabOverlay(event: TabOverlayEvent) {
        event.cancel()
    }

}