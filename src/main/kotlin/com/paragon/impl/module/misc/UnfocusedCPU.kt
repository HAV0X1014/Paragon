package com.paragon.impl.module.misc

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.GetFramerateLimitEvent
import com.paragon.impl.event.render.RenderWorldEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import org.lwjgl.opengl.Display

/**
 * @author Surge
 * @since 20/11/2022
 */
object UnfocusedCPU : Module("UnfocusedCPU", Category.MISC, "Lowers the FPS limit when the window isn't focused") {

    private val limit = Setting("Limit", 5.0, 1.0, 60.0, 1.0) describedBy "The maximum FPS to allow whilst the window is unfocused"
    private val cancelRenderWorld = Setting("CancelRenderWorld", true) describedBy "Cancel rendering the world"

    @Listener
    fun onGetFramerateLimit(event: GetFramerateLimitEvent) {
        if (!Display.isActive()) {
            event.limit = limit.value.toInt()
            event.cancel()
        }
    }

    @Listener
    fun onRenderWorld(event: RenderWorldEvent) {
        if (cancelRenderWorld.value && !Display.isActive()) {
            event.cancel()
        }
    }

}