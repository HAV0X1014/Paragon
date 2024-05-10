package com.paragon.impl.module.movement

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.PlayerUtil

/**
 * @author Surge
 * @since 03/12/2022
 */
object TickShift : Module("TickShift", Category.MOVEMENT, "Increases your speed after standing still") {

    private val ticks = Setting("Ticks", 17.0, 1.0, 50.0, 1.0) describedBy "How many ticks to wait for and apply the boost to"
    private val timer = Setting("Timer", 1.37f, 1.0f, 3.0f, 0.01f) describedBy "What to set the timer speed to"

    // The amount of ticks we have waited for
    private var tickCount = 0

    override fun onEnable() {
        tickCount = 0
    }

    override fun onDisable() {
        if (mc.anyNull) {
            return
        }

        // Reset ticks
        ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f)
    }

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        // We have exceeded the amount of ticks we want to apply the boost to
        if (tickCount <= 0) {
            // Reset
            tickCount = 0

            // Reset timer speed
            ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f)
        }

        if (PlayerUtil.isMoving) {
            if (tickCount > 0) {
                // Decrease tick count
                this.tickCount--

                // Set timer speed
                ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f / timer.value)
            } else {
                // Reset timer speed
                ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f)
            }
        } else {
            // Increase ticks as long as it isn't above our limit
            if (tickCount < ticks.value) {
                this.tickCount++
            }
        }
    }

    override fun getData() = tickCount.toString()

}