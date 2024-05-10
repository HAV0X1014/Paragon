package com.paragon.util.player

import com.paragon.mixins.accessor.IMinecraft
import com.paragon.mixins.accessor.ITimer
import com.paragon.util.mc
import com.paragon.util.world.BlockUtil.getBlockAtPos
import net.minecraft.client.renderer.EnumFaceDirection
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.init.MobEffects
import net.minecraft.item.EnumAction
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object PlayerUtil {

    /**
     * Stops all movements of the player on the X/Y axis.
     */
    @JvmStatic
    fun stopMotion(fallSpeed: Float) = mc.player.setVelocity(0.0, fallSpeed.toDouble(), 0.0)

    val isCollided: Boolean
        get() = mc.player.collidedHorizontally || mc.player.collidedVertically

    val isInLiquid: Boolean
        get() = mc.player.isInWater || mc.player.isInLava

    @JvmStatic
    fun lockLimbs() {
        mc.player.prevLimbSwingAmount = 0f
        mc.player.limbSwingAmount = 0f
        mc.player.limbSwing = 0f
    }

    val isMoving: Boolean
        get() = mc.player.movementInput.moveForward != 0f || mc.player.movementInput.moveStrafe != 0f || mc.player.posX != mc.player.lastTickPosX || mc.player.posZ != mc.player.lastTickPosZ

    @JvmStatic
    fun move(speed: Float) {
        val mover = if (mc.player.isRiding) mc.player.ridingEntity else mc.player
        var forward = mc.player.movementInput.moveForward
        var strafe = mc.player.movementInput.moveStrafe
        var playerYaw = mc.player.rotationYaw

        if (mover != null) {
            if (forward != 0f) {
                if (strafe >= 1) {
                    playerYaw += (if (forward > 0) -45 else 45).toFloat()
                    strafe = 0f
                }
                else if (strafe <= -1) {
                    playerYaw += (if (forward > 0) 45 else -45).toFloat()
                    strafe = 0f
                }

                if (forward > 0) {
                    forward = 1f
                }
                else if (forward < 0) {
                    forward = -1f
                }
            }

            val sin = sin(Math.toRadians((playerYaw + 90).toDouble()))
            val cos = cos(Math.toRadians((playerYaw + 90).toDouble()))

            mover.motionX = forward.toDouble() * speed * cos + strafe.toDouble() * speed * sin
            mover.motionZ = forward.toDouble() * speed * sin - strafe.toDouble() * speed * cos

            mover.stepHeight = 0.6f

            if (!isMoving) {
                mover.motionX = 0.0
                mover.motionZ = 0.0
            }
        }
    }

    fun forward(speed: Double): Vec3d {
        var forwardInput = mc.player.movementInput.moveForward
        var strafeInput = mc.player.movementInput.moveStrafe
        var playerYaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.renderPartialTicks

        if (forwardInput != 0.0f) {
            if (strafeInput > 0.0f) {
                playerYaw += (if (forwardInput > 0.0f) -45 else 45).toFloat()
            }
            else if (strafeInput < 0.0f) {
                playerYaw += (if (forwardInput > 0.0f) 45 else -45).toFloat()
            }

            strafeInput = 0.0f

            if (forwardInput > 0.0f) {
                forwardInput = 1.0f
            }
            else if (forwardInput < 0.0f) {
                forwardInput = -1.0f
            }
        }

        val sin = sin(Math.toRadians((playerYaw + 90.0f).toDouble()))
        val cos = cos(Math.toRadians((playerYaw + 90.0f).toDouble()))

        val posX = forwardInput * speed * cos + strafeInput * speed * sin
        val posZ = forwardInput * speed * sin - strafeInput * speed * cos

        return Vec3d(posX, mc.player.posY, posZ)
    }

    @JvmStatic
    fun propel(speed: Float) {
        val yaw = mc.player.rotationYaw

        val pitch = mc.player.rotationPitch
        mc.player.motionX -= sin(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * speed
        mc.player.motionZ += cos(Math.toRadians(yaw.toDouble())) * cos(Math.toRadians(pitch.toDouble())) * speed
        mc.player.motionY += -sin(Math.toRadians(pitch.toDouble())) * speed
    }

    val isPlayerEating: Boolean
        get() = mc.player.isHandActive && mc.player.activeItemStack.itemUseAction == EnumAction.EAT

    val isPlayerDrinking: Boolean
        get() = mc.player.isHandActive && mc.player.activeItemStack.itemUseAction == EnumAction.DRINK

    val isPlayerConsuming: Boolean
        get() = mc.player.isHandActive && (mc.player.activeItemStack.itemUseAction == EnumAction.EAT || mc.player.activeItemStack.itemUseAction == EnumAction.DRINK)

    val direction: EnumFaceDirection
        get() = EnumFaceDirection.getFacing(EnumFacing.fromAngle(mc.player.rotationYaw.toDouble()))

    fun getAxis(direction: EnumFaceDirection?) = when (direction) {
        EnumFaceDirection.NORTH -> "-Z"
        EnumFaceDirection.SOUTH -> "+Z"
        EnumFaceDirection.EAST -> "+X"
        EnumFaceDirection.WEST -> "-X"
        else -> ""
    }

    val baseMoveSpeed: Double
        get() = 0.2873 * if (mc.player.isPotionActive(MobEffects.SPEED)) 1.0 + 0.2 * (mc.player.getActivePotionEffect(MobEffects.SPEED)!!.amplifier + 1.0) else 1.0


    /**
     * @return the block under the given [Entity].
     */
    fun getBlockUnder(player: Entity): BlockPos? {
        var pos = BlockPos(player.posX, player.posY, player.posZ)
        while (pos.y > -2 && pos.getBlockAtPos() === Blocks.AIR) {
            pos = pos.down()
        }
        return if (pos.y < 0) null else pos
    }

    fun getSpeed(unit: Unit): Double {
        return unit.algorithm.invoke(hypot(mc.player.posX - mc.player.lastTickPosX, mc.player.posZ - mc.player.lastTickPosZ) * (1000 / ((mc as IMinecraft).hookGetTimer() as ITimer).hookGetTickLength()).toDouble())
    }

    enum class Unit(val algorithm: (Double) -> Double) {
        /**
         * Speed in blocks per second
         */
        BPS({ it }),

        /**
         * Speed in kilometers (1000 blocks) per hour
         */
        KMH({ it * 3.6 }),

        /**
         * Speed in miles (1.60934 km) per hour
         */
        MPH({ it * 2.237 });

    }

}