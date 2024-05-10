package com.paragon.impl.module.movement

import com.paragon.impl.event.player.StepEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.string.StringUtil.getFormattedText
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

/**
 * @author Surge, Doogie13, aesthetical
 * Uses Cosmos's step event, originally from auto which Aestheticall implemented into Cosmos with auto's permission
 */
object Step : Module("Step", Category.MOVEMENT, "Lets you instantly step up blocks") {

    // Step mode
    private val mode = Setting("Mode", Mode.NCP) describedBy "What mode to use"

    // Vanilla step height
    private val stepHeight = Setting(
        "StepHeight", 1.5f, 0.5f, 2.5f, 0.5f
    ) describedBy "How high to step up"
    private val useTimer = Setting(
        "UseTimer", true
    ) describedBy "If to use timer to prevent the MORE_PACKETS flag on NCP" visibleWhen { mode.value == Mode.NCP }

    private val ncpOffsets: Map<Double, DoubleArray> = hashMapOf(
        0.875 to doubleArrayOf(0.39, 0.7, 0.875),
        1.0 to doubleArrayOf(0.42, 0.75, 1.0),
        1.5 to doubleArrayOf(0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43),
        2.0 to doubleArrayOf(0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.919)
    )

    private var timer = false

    override fun onDisable() {
        if (mc.anyNull) {
            return
        }

        // Set step height to normal
        mc.player.stepHeight = 0.6f

        // reset our tickLength to 50.0f (1 timer speed)
        ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50.0f)
        timer = false
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent?) {
        if (mc.anyNull) {
            return
        }

        // Set step height
        mc.player.stepHeight = stepHeight.value
    }

    @SubscribeEvent
    fun onUpdate(event: LivingUpdateEvent) {
        if (event.entityLiving == mc.player) {

            // if we have used timer before and we are on ground after stepping, reset our timer
            if (timer && mc.player.onGround) {
                timer = false
                ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50.0f)
            }
        }
    }

    @Listener
    fun onStep(event: StepEvent) {
        if (mode.value == Mode.NCP && event.entity == mc.player && !mc.player.capabilities.isFlying) {
            val height: Double = event.bB.minY - mc.player.posY

            // don't step if there are any flagging conditions
            if (height > stepHeight.value || !mc.player.onGround || mc.player.isInWater || mc.player.isInLava) {
                return
            }

            // get our packet offsets from the map, and then a default value of null
            val offsets = ncpOffsets.getOrDefault(height, null)

            if (offsets == null || offsets.isEmpty()) {
                return
            }

            if (useTimer.value) {
                // set our timer dynamically based off of the amount of offsets we are using
                ((mc as IMinecraft).hookGetTimer() as ITimer).hookSetTickLength(50.0f / (1.0f / (offsets.size + 1.0f)))
                timer = true
            }

            // Send offsets - this simulates a fake jump
            for (offset in offsets) {
                mc.player.connection.sendPacket(
                    CPacketPlayer.Position(
                        mc.player.posX, mc.player.posY + offset, mc.player.posZ, false
                    )
                )
            }
        }
    }

    override fun getData(): String {
        return getFormattedText(mode.value)
    }

    @Suppress("UNUSED")
    enum class Mode {
        /**
         * Vanilla step - bypasses almost no servers :P
         */
        VANILLA,

        /**
         * NCP step - bypasses the NCP anticheat
         *
         * Is notable that some step heights over 1.5 are not possible on Strict NCP
         */
        NCP
    }

}