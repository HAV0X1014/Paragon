package com.paragon.impl.module.misc

import com.paragon.Paragon
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.managers.rotation.Rotation
import com.paragon.impl.managers.rotation.RotationPriority
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import com.paragon.util.player.RotationUtil
import com.paragon.util.world.BlockUtil
import com.paragon.util.world.BlockUtil.getBlockAtPos
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.math.BlockPos

/**
 * @author SooStrator1136
 */
object Lawnmower : Module("Lawnmower", Category.MISC, "Removes grass and flowers") {

    private val range = Setting(
        "Range", 4F, 1F, 7F, 0.1F
    ) describedBy "The range in which to remove lawn"

    private val delay = Setting("Delay", 50.0, 50.0, 1000.0, 50.0)

    private val rotateMode = Setting("Rotation", Rotate.NONE)

    private val lawnBlocks = arrayOf(
        Blocks.TALLGRASS,
        Blocks.RED_FLOWER,
        Blocks.DOUBLE_PLANT,
        Blocks.YELLOW_FLOWER,
    )
    private val timer = Timer()
    private val toRemove = ArrayDeque<BlockPos>()

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        toRemove.addAll(BlockUtil.getSphere(range.value, true).filter {
            lawnBlocks.contains(it.getBlockAtPos()) && !toRemove.contains(it)
        })

        toRemove.filter {
            BlockUtil.canSeePos(it) && it.getDistance(
                mc.player.posX.toInt(), (mc.player.posY + mc.player.getEyeHeight()).toInt(), mc.player.posZ.toInt()
            ) <= range.value
        }

        if (timer.hasMSPassed(delay.value) && toRemove.isNotEmpty()) {
            val blockPos = toRemove.removeFirst()
            val rotation = RotationUtil.getRotationToBlockPos(blockPos, 0.5)

            if (rotateMode.value == Rotate.LEGIT) {
                Paragon.INSTANCE.rotationManager.addRotation(
                    Rotation(
                        rotation.x, rotation.y, rotateMode.value, RotationPriority.HIGH
                    )
                )
            }
            else if (rotateMode.value == Rotate.PACKET) {
                mc.player.connection.sendPacket(
                    CPacketPlayer.Rotation(
                        rotation.x, rotation.y, mc.player.onGround
                    )
                )
            }

            mc.objectMouseOver.sideHit

            mc.player.connection.sendPacket(
                CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, BlockUtil.getFacing(blockPos) ?: return
                )
            )

            timer.reset()
        }
    }

    override fun onDisable() {
        toRemove.clear()
    }

}