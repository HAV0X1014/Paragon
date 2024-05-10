package com.paragon.util.player

import com.paragon.impl.managers.rotation.Rotate
import com.paragon.util.mc
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec2f
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import kotlin.math.atan2
import kotlin.math.hypot

@SideOnly(Side.CLIENT)
object RotationUtil {

    /**
     * Gets the rotation to a block position
     *
     * @param pos The block position to calculate angles to
     * @param yOffset The y offset to use
     * @return The calculated angles
     */
    @JvmStatic
    fun getRotationToBlockPos(pos: BlockPos, yOffset: Double): Vec2f {
        return getRotationToVec3d(Vec3d(pos.x + 0.5, pos.y + yOffset, pos.z + 0.5))
    }

    /**
     * Gets the yaw and pitch to rotate to a [Vec3d].
     *
     * @param vec3d The [Vec3d] to calculate rotations to
     * @return A [Vec2f] of the angles
     */
    @JvmStatic
    fun getRotationToVec3d(vec3d: Vec3d): Vec2f {
        val yaw = (Math.toDegrees(
            atan2(
                vec3d.subtract(mc.player.getPositionEyes(1f)).z,
                vec3d.subtract(mc.player.getPositionEyes(1f)).x
            )
        ) - 90).toFloat()
        val pitch = Math.toDegrees(
            -atan2(
                vec3d.subtract(mc.player.getPositionEyes(1f)).y,
                hypot(
                    vec3d.subtract(mc.player.getPositionEyes(1f)).x,
                    vec3d.subtract(mc.player.getPositionEyes(1f)).z
                )
            )
        ).toFloat()

        return Vec2f(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch))
    }

    fun rotate(rotationVec: Vec2f, mode: Rotate) {
        if (mode === Rotate.NONE) {
            return
        }

        rotate(rotationVec, mode === Rotate.PACKET)
    }

    fun rotate(rotationVec: Vec2f, packet: Boolean) {
        mc.player.connection.sendPacket(
            CPacketPlayer.Rotation(
                rotationVec.x, rotationVec.y, mc.player.onGround
            )
        )

        if (packet) {
            return
        }

        mc.player.rotationYaw = rotationVec.x
        mc.player.rotationYawHead = rotationVec.x
        mc.player.rotationPitch = rotationVec.y
    }

    fun normalizeAngle(angle: Float): Float {
        var normalizedAngle = angle

        normalizedAngle %= 360.0f

        if (normalizedAngle >= 180.0f) {
            normalizedAngle -= 360.0f
        }

        if (normalizedAngle < -180.0f) {
            normalizedAngle += 360.0f
        }

        return normalizedAngle
    }

}