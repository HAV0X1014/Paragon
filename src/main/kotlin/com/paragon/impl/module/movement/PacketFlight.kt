package com.paragon.impl.module.movement

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.event.player.PlayerMoveEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.mixins.accessor.ISPacketPlayerPosLook
import com.paragon.util.mc
import net.minecraft.network.play.client.CPacketConfirmTeleport
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d
import kotlin.math.*


/**
 * @author Doogie13, Surge
 * @since 20/12/2022
 */
object PacketFlight : Module("PacketFlight", Category.MOVEMENT, "Allows you to phase through blocks") {

    private val factor = Setting("Factor", 1.3f, 0.1f, 5f, 0.1f)

    private var teleportID = -1
    private val allowedPackets = HashSet<CPacketPlayer>()
    private val allowedPositionsAndIDs = hashMapOf<Int, Vec3d>()

    override fun onEnable() {
        this.teleportID = -1
    }

    override fun onDisable() {
        allowedPackets.clear()
        allowedPositionsAndIDs.clear()
    }

    @Listener
    fun onPlayerMove(event: PlayerMoveEvent) {
        var motionX: Double
        var motionY = 0.0
        var motionZ: Double

        var antiKicking = false

        if (mc.player.ticksExisted % 10 == 0 && !mc.world.collidesWithAnyBlock(mc.player.entityBoundingBox)) {
            motionY = -0.04
            antiKicking = true
        } else {
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                motionY = 0.0624
            } else if (mc.gameSettings.keyBindSneak.isKeyDown) {
                motionY = -0.0624
            }
        }

        var motionH: Double

        var walls = mc.world.collidesWithAnyBlock(mc.player.entityBoundingBox)

        if (walls) {
            motionH = 0.0624

            if (motionY != 0.0) {
                val multiply = 1 / sqrt(2.0)

                motionY *= multiply
                motionH *= multiply
            }
        } else {
            motionH = 0.2873

            val movingHorizontally = mc.player.moveForward != 0f || mc.player.moveStrafing != 0f

            if (movingHorizontally) {
                motionY = min(0.0, motionY)
            }
        }

        var dir = doubleArrayOf(0.0, 0.0)

        if (!(mc.player.moveForward == 0f && mc.player.moveStrafing == 0f)) {
            var strafing = 0
            var forward = 0

            if (mc.player.moveStrafing < 0) {
                strafing = -1
            } else if (mc.player.moveStrafing > 0) {
                strafing = 1
            }

            if (mc.player.moveForward < 0) {
                forward = -1
            } else if (mc.player.moveForward > 0) {
                forward = 1
            }

            var strafe = (90 * strafing).toFloat()
            strafe *= if (forward.toFloat() != 0f) forward * 0.5f else 1f

            var yaw: Float = mc.player.rotationYaw - strafe
            yaw -= (if (mc.player.moveForward < 0f) 180 else 0).toFloat()

            yaw *= (1 / (180 / Math.PI)).toFloat()

            val x = -sin(yaw.toDouble()) * motionH
            val z = cos(yaw.toDouble()) * motionH

            dir = doubleArrayOf(x, z)
        }

        motionX = dir[0]
        motionZ = dir[1]

        var factorInt = floor(factor.value.toDouble()).toInt()

        if (mc.player.ticksExisted % 10 < 10 * (factor.value - factorInt)) {
            factorInt++
        }

        val motion = send(motionX, motionY, motionZ, antiKicking, factorInt)

        event.x = motion.x
        event.y = motion.y
        event.z = motion.z

        mc.player.noClip = true
    }

    private fun send(motionX: Double, motionY: Double, motionZ: Double, antiKick: Boolean, factor: Int): Vec3d {
        var motionY = motionY
        for (i in 1 .. factor + 1) {

            if (antiKick && factor != 1) {
                motionY = 0.0
            }

            val pos: Vec3d = mc.player.positionVector.add(Vec3d(motionX * i, motionY * i, motionZ * i))

            val packet = CPacketPlayer.Position(pos.x, pos.y, pos.z, true)

            val bounds = CPacketPlayer.Position(pos.x, pos.y + 512, pos.z, true)

            allowedPackets.add(packet)
            allowedPackets.add(bounds)

            mc.player.connection.sendPacket(packet)
            mc.player.connection.sendPacket(bounds)

            if (teleportID < 0) {
                break
            }

            teleportID++

            mc.player.connection.sendPacket(CPacketConfirmTeleport(teleportID))

            allowedPositionsAndIDs[teleportID] = pos
        }

        return Vec3d(motionX * factor, motionY * if (antiKick) 1 else factor, motionZ * factor)
    }

    @Listener
    fun onPacketSend(event: PacketEvent.PreSend) {
        if (event.packet is CPacketPlayer) {
            if (!allowedPackets.contains(event.packet)) {
                event.cancel()
            }
        }
    }

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        if (event.packet is SPacketPlayerPosLook) {
            val id = event.packet.teleportId

            if (allowedPositionsAndIDs.containsKey(id)) {
                if (allowedPositionsAndIDs[id]!! == Vec3d(event.packet.x, event.packet.y, event.packet.z)) {
                    allowedPositionsAndIDs.remove(id)

                    mc.player.connection.sendPacket(CPacketConfirmTeleport(id))

                    event.cancel()
                    return
                }
            }

            teleportID = id

            (event.packet as ISPacketPlayerPosLook).hookSetYaw(mc.player.rotationYaw)
            (event.packet as ISPacketPlayerPosLook).hookSetPitch(mc.player.rotationPitch)

            mc.player.connection.sendPacket(CPacketConfirmTeleport(id))
        }
    }

}