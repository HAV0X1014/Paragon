package com.paragon.impl.module.render

import com.paragon.impl.event.render.AspectEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category

/**
 * @author Surge
 * @since 23/05/22
 */
object AspectRatio : Module("AspectRatio", Category.RENDER, "Changes the aspect ratio of the game") {

    private val ratio = Setting(
        "Ratio", 1f, 0.5f, 10f, 0.01f
    ) describedBy "The ratio of the screen"

    @Listener
    fun onAspectRatioEvent(event: AspectEvent) {
        event.cancel()
        event.ratio = ratio.value
    }

}