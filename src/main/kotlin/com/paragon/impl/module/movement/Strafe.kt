package com.paragon.impl.module.movement

import com.paragon.impl.event.player.PlayerMoveEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.player.PlayerUtil
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import net.minecraft.util.math.Vec3d
import kotlin.math.max

/**
 * @author Surge
 * @since 15/07/22
 */
@Aliases(["Speed"])
object Strafe : Module("Strafe", Category.MOVEMENT, "Increases your movement speed") {

    private val timerSpeed = Setting(
        "TimerSpeed", 1.09f, 1.01f, 2f, 0.01f
    ) describedBy "How fast the game's timer will tick at"

    private val airSpeed = Setting(
        "AirSpeed", 1.05f, 1f, 2f, 0.01f
    ) describedBy "How fast the game's timer will tick at when you're in the air"

    private val speedFactor = Setting(
        "SpeedFactor", 1.25f, 1f, 2f, 0.01f
    ) describedBy "How much to multiply the speed by"

    private val airFriction = Setting(
        "AirFriction", 0.5f, 0.1f, 1f, 0.1f
    ) describedBy "How much friction to apply whilst in the air"

    private val delay = Setting(
        "Delay", 250.0, 0.0, 1000.0, 1.0
    ) describedBy "How long to wait before increasing the speed"

    private var state: State = State.INCREASE
    private var speed: Double = 0.0
    private var timer: Timer = Timer()

    override fun onEnable() {
        state = State.SLOW

        if (!mc.anyNull) {
            speed = PlayerUtil.baseMoveSpeed
        }
    }

    override fun onDisable() {
        setTimerSpeed(1f)
    }

    @Listener
    fun onMove(event: PlayerMoveEvent) {
        if (mc.player.ticksExisted < 1) {
            speed = PlayerUtil.baseMoveSpeed
        }

        // Don't apply speed if we're in a liquid or on a ladder
        if (applySpeed()) {

            // Make sure we are moving
            if (PlayerUtil.isMoving) {
                // Check on ground state and whether the delay has passed
                if (mc.player.onGround && timer.hasMSPassed(delay.value)) {
                    // Increase timer speed
                    setTimerSpeed(timerSpeed.value)

                    // Simulate a jump, but with 0.41 instead of 0.42 as it seems to bypass better?
                    event.y = ((0.41f).toFloat().also { mc.player.motionY = it.toDouble() }).toDouble()

                    // Set speed
                    speed = PlayerUtil.baseMoveSpeed * speedFactor.value.toDouble()

                    // Set state
                    state = State.SLOW

                    // Reset timer
                    timer.reset()
                }
                else {
                    // Set timer speed to the air speed
                    setTimerSpeed(airSpeed.value)

                    // Check state or horizontal collision state
                    if (state == State.SLOW || mc.player.collidedHorizontally) {
                        // Decrease speed
                        speed -= airFriction.value * PlayerUtil.baseMoveSpeed

                        // Set state
                        state = State.INCREASE
                    }
                }

                // Get the greatest possible speed
                speed = max(speed, PlayerUtil.baseMoveSpeed)

                // Get vector of forward position
                val forward: Vec3d = PlayerUtil.forward(speed)

                // Override X and Y
                event.x = forward.x
                event.z = forward.z
            }
        }
    }

    private fun applySpeed(): Boolean {
        return !mc.player.isInLava && !mc.player.isInWater && !mc.player.isOnLadder
    }

    private fun setTimerSpeed(input: Float) {
        ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f / input)
    }

    enum class State {
        /**
         * Boost
         */
        INCREASE,

        /**
         * Slow down
         */
        SLOW
    }

}