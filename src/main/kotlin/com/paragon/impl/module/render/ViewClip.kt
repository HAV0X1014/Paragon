package com.paragon.impl.module.render

import com.paragon.impl.event.render.entity.CameraClipEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category

/**
 * @author Surge
 * @since 14/05/22
 */
object ViewClip : Module("ViewClip", Category.RENDER, "Lets your third person view clip to the world") {

    var distance = Setting(
        "Distance", 10.0, 1.0, 20.0, 0.1
    ) describedBy "How far your camera is from your body"

    @Listener
    fun onCameraClip(event: CameraClipEvent) {
        event.cancel()
        event.distance = distance.value
    }

}