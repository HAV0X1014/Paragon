package com.paragon.impl.module.combat

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.player.PlayerUtil
import com.paragon.impl.managers.rotation.Rotate
import com.paragon.impl.managers.rotation.Rotation
import com.paragon.impl.managers.rotation.RotationPriority
import com.paragon.impl.module.Category
import com.paragon.mixins.accessor.IEntity
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.InventoryUtil
import com.paragon.util.player.PlacementUtil
import com.paragon.util.player.RotationUtil
import com.paragon.util.world.BlockUtil
import com.paragon.util.world.BlockUtil.distanceToEyes
import com.paragon.util.world.BlockUtil.getBlockAtPos
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

/**
 * 我寫這篇文章的時候已經很晚了
 *
 * @author SooStrator1136
 */
object WebAura : Module("WebAura", Category.COMBAT, "Spiderman on drugs wtf") {

    private val mode = Setting("TargetMode", TargetMode.OTHERS)

    private val rotationMode = Setting("Rotation", Rotate.NONE)

    private val onlyHole = Setting(
        "Only holes", false
    ) describedBy "Only places webs when you are in a hole" visibleWhen { mode.value == TargetMode.SELF }

    private val range = Setting(
        "Range", 5F, 2F, 7F, 0.5F
    ) describedBy "Range to place webs in" visibleWhen { mode.value == TargetMode.OTHERS }

    private val packetOnItemChange = Setting("SyncItem", true)

    private val itemCobweb = Item.getItemFromBlock(Blocks.WEB)

    override fun onTick() {
        if (mc.anyNull || InventoryUtil.getItemInHotbar(itemCobweb) == -1) {
            return
        }

        when (mode.value) {
            TargetMode.SELF -> {
                val playerBlock = BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)
                if ((onlyHole.value && !BlockUtil.isHole(playerBlock)) || playerBlock.getBlockAtPos() != Blocks.AIR) {
                    return
                }

                val placeRotation = RotationUtil.getRotationToBlockPos(playerBlock, 0.5)
                PlacementUtil.place(
                    playerBlock, Rotation(placeRotation.x, placeRotation.y, rotationMode.value, RotationPriority.HIGH), getWebHand()
                )
                switchBack()
            }

            TargetMode.OTHERS -> {
                val possibleTargets = mc.world.loadedEntityList.filter {
                    !it.isDead && it != mc.player && it is EntityPlayer && !(it as IEntity).hookIsInWeb()
                }.mapNotNull {
                    val blockUnder = PlayerUtil.getBlockUnder(it)
                    return@mapNotNull if (blockUnder == null || blockUnder.distanceToEyes > range.value) {
                        null
                    }
                    else {
                        blockUnder
                    }
                }.also {
                    if (it.isEmpty()) {
                        return
                    }
                }

                val placePos = possibleTargets.minWith(Comparator.comparingDouble { it.distanceToEyes })

                RotationUtil.rotate(RotationUtil.getRotationToBlockPos(placePos, 0.5), rotationMode.value)
                mc.playerController.processRightClickBlock(
                    mc.player, mc.world, placePos, EnumFacing.UP, Vec3d(0.5, 0.5, 0.5), getWebHand()
                )
                mc.player.swingArm(EnumHand.MAIN_HAND)
                switchBack()
            }
        }
    }

    private var previousSlot = 0

    private fun getWebHand(): EnumHand { //Who puts  webs in their offhand cmon
        previousSlot = mc.player.inventory.currentItem
        mc.player.inventory.currentItem = InventoryUtil.getItemInHotbar(itemCobweb)
        if (packetOnItemChange.value) {
            mc.connection?.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
        }

        return EnumHand.MAIN_HAND
    }

    private fun switchBack() {
        mc.player.inventory.currentItem = previousSlot
        if (packetOnItemChange.value) {
            mc.connection?.sendPacket(CPacketHeldItemChange(previousSlot))
        }
    }

    internal enum class TargetMode {
        SELF, OTHERS
    }

}