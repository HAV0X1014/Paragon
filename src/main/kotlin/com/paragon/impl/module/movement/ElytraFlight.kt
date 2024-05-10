package com.paragon.impl.module.movement

import com.paragon.impl.event.player.TravelEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.player.PlayerUtil.lockLimbs
import com.paragon.util.player.PlayerUtil.move
import com.paragon.util.player.PlayerUtil.propel
import com.paragon.util.player.PlayerUtil.stopMotion
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.string.StringUtil.getFormattedText
import net.minecraft.network.play.client.CPacketEntityAction

/**
 * @author Surge
 */
object ElytraFlight : Module("ElytraFlight", Category.MOVEMENT, "Allows for easier flight with an elytra") {

    // Mode for elytra flight
    private val mode = Setting("Mode", Mode.CONTROL) describedBy "The mode to use"

    // Strict settings
    private val ascendPitch = Setting(
        "AscendPitch", -45f, -90f, 90f, 1f
    ) describedBy "What value to set your pitch to when ascending" subOf mode visibleWhen { mode.value == Mode.STRICT }
    private val descendPitch = Setting(
        "DescendPitch", 45f, -90f, 90f, 1f
    ) describedBy "What value to set your pitch to when descending" subOf mode visibleWhen { mode.value == Mode.STRICT }
    private val lockPitch = Setting(
        "LockPitch", true
    ) describedBy "Lock your pitch when you are not ascending or descending" subOf mode visibleWhen { mode.value == Mode.STRICT }
    private val lockPitchVal = Setting(
        "LockedPitch", 0f, -90f, 90f, 1f
    ) describedBy "The pitch to lock you to when you are not ascending or descending" subOf mode visibleWhen { mode.value == Mode.STRICT }

    // Boost settings
    private val cancelMotion = Setting(
        "CancelMotion", false
    ) describedBy "Stop motion when not moving" subOf mode visibleWhen { mode.value == Mode.BOOST }

    // Global settings
    private val flySpeed = Setting("FlySpeed", 1f, 0.1f, 2f, 0.1f) describedBy "The speed to fly at"
    private val ascend = Setting(
        "AscendSpeed", 1.0, 0.1, 2.0, 0.1
    ) describedBy "How fast to ascend" visibleWhen { mode.value != Mode.BOOST }
    private val descend = Setting(
        "DescendSpeed", 1.0, 0.1, 2.0, 0.1
    ) describedBy "How fast to descend" visibleWhen { mode.value != Mode.BOOST }
    private val fallSpeed = Setting("FallSpeed", 0f, 0f, 0.1f, 0.01f) describedBy "How fast to fall"

    // Takeoff settings
    private val takeOff = Setting("Takeoff", false) describedBy "Automatically take off when you enable the module"
    private val takeOffTimer = Setting(
        "Timer", 0.2f, 0.1f, 1f, 0.1f
    ) describedBy "How long a tick lasts for" subOf takeOff

    override fun onEnable() {
        if (mc.anyNull) {
            return
        }

        if (takeOff.value) {
            // Make sure we aren't elytra flying
            if (!mc.player.isElytraFlying) {

                // Make the game slower
                ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50 / takeOffTimer.value)

                if (mc.player.onGround) {
                    // Jump if we're on the ground
                    mc.player.jump()
                }
                else {
                    // Make us fly if we are off the ground
                    mc.player.connection.sendPacket(
                        CPacketEntityAction(
                            mc.player, CPacketEntityAction.Action.START_FALL_FLYING
                        )
                    )
                }
            }
        }
    }

    override fun onDisable() {
        // Set us back to normal speed
        ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f)
    }

    @Listener
    fun onTravel(travelEvent: TravelEvent) {
        if (mc.anyNull) {
            return
        }

        if (mc.player.isElytraFlying) {

            // Set us to normal speed if we are flying
            ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50f)
            if (mode.value != Mode.BOOST) {
                // Cancel motion
                travelEvent.cancel()

                // Make us fall
                stopMotion(-fallSpeed.value)
            }
            else {
                if (cancelMotion.value) {
                    // Cancel motion
                    travelEvent.cancel()

                    // Make us fall
                    stopMotion(-fallSpeed.value)
                }
            }

            when (mode.value) {
                Mode.CONTROL -> {
                    // Move
                    move(flySpeed.value)

                    // Handle moving up and down
                    handleControl()
                }

                Mode.STRICT -> {
                    // Move
                    move(flySpeed.value)

                    // Handle moving up and down
                    handleStrict()
                }

                Mode.BOOST -> if (mc.gameSettings.keyBindForward.isKeyDown && !(mc.player.posX - mc.player.lastTickPosX > flySpeed.value || mc.player.posZ - mc.player.lastTickPosZ > flySpeed.value)) {
                    // Move forward
                    propel(flySpeed.value * if (cancelMotion.value) 1f else 0.015f)
                }
            }

            // Lock our limbs
            lockLimbs()
        }
    }

    private fun handleControl() {
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            // Increase Y
            mc.player.motionY = ascend.value
        }

        if (mc.gameSettings.keyBindSneak.isKeyDown) {
            // Decrease Y
            mc.player.motionY = -descend.value
        }
    }

    private fun handleStrict() {
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            // Increase pitch
            mc.player.rotationPitch = ascendPitch.value

            // Increase Y
            mc.player.motionY = ascend.value
        }
        else if (mc.gameSettings.keyBindSneak.isKeyDown) {
            // Decrease pitch
            mc.player.rotationPitch = descendPitch.value

            // Decrease Y
            mc.player.motionY = -descend.value
        }
        else {
            if (lockPitch.value) {
                // Set pitch if we aren't moving
                mc.player.rotationPitch = lockPitchVal.value
            }
        }
    }

    override fun getData() = getFormattedText(mode.value)

    enum class Mode {
        /**
         * Lets you fly without idle gliding
         */
        CONTROL,

        /**
         * Lets you fly on strict servers
         */
        STRICT,

        /**
         * Boost yourself when using an elytra
         */
        BOOST
    }

}