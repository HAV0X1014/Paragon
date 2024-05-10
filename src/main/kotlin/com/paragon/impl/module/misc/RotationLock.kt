package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc

/**
 * @author Surge
 */
object RotationLock : Module("RotationLock", Category.MISC, "Locks your rotation") {

    private val yaw = Setting(
        "Yaw", 0f, -180f, 180f, 1f
    ) describedBy "The yaw to lock to"

    private val pitch = Setting(
        "Pitch", 0f, -180f, 180f, 1f
    ) describedBy "The pitch to lock to"

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        mc.player.rotationYaw = yaw.value
        mc.player.rotationYawHead = yaw.value
        mc.player.rotationPitch = pitch.value
    }

}