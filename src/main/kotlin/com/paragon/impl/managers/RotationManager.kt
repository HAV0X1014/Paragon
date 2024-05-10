package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.managers.rotation.Rotation
import com.paragon.mixins.accessor.ICPacketPlayer
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.RotationUtil.normalizeAngle
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.Vec2f
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs


/**
 * @author Surge
 */
class RotationManager {

    private val rotationsQueue = CopyOnWriteArrayList<Rotation>()
    private var packetYaw = -1f
    private var packetPitch = -1f

    var serverRotation = Vec2f(-1f, -1f)

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.eventBus.register(this)
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (mc.anyNull) {
            rotationsQueue.clear()
            return
        }

        rotationsQueue.removeIf { it.rotate == Rotate.NONE }

        if (rotationsQueue.isNotEmpty()) {

            rotationsQueue.sortBy { it.priority.priority }

            val rotation = rotationsQueue[0]

            if (rotation.yaw == serverRotation.x && rotation.pitch == serverRotation.y) {
                rotationsQueue.clear()
                return
            }

            // We use server rotation because it will be updated whether the mode is packet or not
            packetYaw = calculateAngle(serverRotation.x, rotation.yaw, rotation.threshold)
            packetPitch = calculateAngle(serverRotation.y, rotation.pitch, 180f)

            if (rotation.rotate == Rotate.LEGIT) {
                mc.player.rotationYaw = packetYaw
                mc.player.rotationYawHead = packetYaw
                mc.player.rotationPitch = packetPitch
            }

            mc.player.connection.sendPacket(CPacketPlayer.Rotation(packetYaw, packetPitch, mc.player.onGround))

            rotationsQueue.clear()
        }
    }

    @Listener
    fun onPacketSend(event: PreSend) {
        if (event.packet is CPacketPlayer.Rotation) {
            serverRotation = Vec2f((event.packet as ICPacketPlayer).hookGetYaw(), (event.packet as ICPacketPlayer).hookGetPitch())
        }
    }

    fun addRotation(rotation: Rotation) = rotationsQueue.add(rotation).let { return@let }

    private fun calculateAngle(playerAngle: Float, wantedAngle: Float, threshold: Float): Float {
        var distance = wantedAngle - playerAngle

        if (abs(distance) > 180) {
            distance = normalizeAngle(distance)
        }

        return if (abs(distance) > threshold) {
            normalizeAngle(playerAngle + threshold * if (distance > 0) 1 else -1)
        } else {
            wantedAngle
        }
    }

}