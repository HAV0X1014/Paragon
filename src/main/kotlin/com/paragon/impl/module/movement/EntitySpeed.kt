package com.paragon.impl.module.movement

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import kotlin.math.cos
import kotlin.math.sin

object EntitySpeed : Module("EntitySpeed", Category.MOVEMENT, "Allows entities to go faster") {

    private val speed = Setting(
        "Speed", 0.5f, 0.1f, 2f, 0.05f
    ) describedBy "How fast the entity goes"


    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        if (mc.player.ridingEntity != null) {
            val movementInput = mc.player.movementInput
            var forward = movementInput.moveForward.toDouble()
            var strafe = movementInput.moveStrafe.toDouble()
            var yaw = mc.player.rotationYaw

            if (forward == 0.0 && strafe == 0.0) {
                mc.player.ridingEntity!!.motionX = 0.0
                mc.player.ridingEntity!!.motionZ = 0.0
            }
            else {
                if (forward != 0.0) {
                    if (strafe > 0.0) {
                        yaw += (if (forward > 0.0) -45 else 45).toFloat()
                    }
                    else if (strafe < 0.0) {
                        yaw += (if (forward > 0.0) 45 else -45).toFloat()
                    }

                    strafe = 0.0
                    if (forward > 0.0) {
                        forward = 1.0
                    }
                    else if (forward < 0.0) {
                        forward = -1.0
                    }
                }

                val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
                val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))

                mc.player.ridingEntity!!.motionX = forward * speed.value * cos + strafe * speed.value * sin
                mc.player.ridingEntity!!.motionZ = forward * speed.value * sin - strafe * speed.value * cos
            }
        }
    }
}
