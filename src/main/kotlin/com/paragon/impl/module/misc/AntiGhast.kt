package com.paragon.impl.module.misc

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import com.paragon.util.player.RotationUtil
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand

/**
 * @author SooStrator1136
 */
object AntiGhast : Module("AntiGhast", Category.MISC, "Keep yourself save (kys) from fireballs") {

    private val rotation = Setting("Rotation", Rotate.NONE)

    private val range = Setting(
        "Range", 3.0, 2.0, 7.0, 0.1
    ) describedBy "The range to attack Fireballs in"

    private val delay = Setting(
        "Delay", 50F, 50F, 250F, 10F
    ) describedBy "The minimum delay between hitting fireballs"

    private val hitType = Setting("HitType", AttackType.NORMAL)

    private val timer = Timer()

    override fun onTick() {
        if (!timer.hasMSPassed(delay.value.toDouble()) || mc.anyNull) {
            return
        }

        val fireBalls = mc.world.loadedEntityList.filterIsInstance<EntityFireball>().filter {
            it.getDistance(
                mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ
            ) <= range.value
        }
        if (fireBalls.isEmpty()) {
            return
        }

        val target = fireBalls.sortedBy {
            it.getDistanceSq(
                mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ
            )
        }[0]

        RotationUtil.rotate(RotationUtil.getRotationToVec3d(target.positionVector), rotation.value)
        if (hitType.value == AttackType.NORMAL) {
            mc.playerController.attackEntity(mc.player, target)
        }
        else {
            mc.connection?.sendPacket(CPacketUseEntity(target))
            mc.connection?.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        }
    }

    internal enum class AttackType {
        NORMAL, PACKET
    }

}