package com.paragon.impl.module.movement

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc

object Flight : Module("Flight", Category.MOVEMENT, "Allows you to fly in survival mode") {

    private val flySpeed = Setting("FlySpeed", 0.05f, 0.01f, 0.1f, 0.01f) describedBy "How fast you fly"

    override fun onDisable() {
        mc.player.capabilities.flySpeed = 0.05f
        mc.player.capabilities.isFlying = false
    }

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        mc.player.capabilities.flySpeed = flySpeed.value
        mc.player.capabilities.isFlying = true
    }

}