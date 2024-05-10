package com.paragon.impl.module.movement

import com.paragon.impl.event.network.PacketEvent.PostSend
import com.paragon.impl.event.network.PacketEvent.PreSend
import com.paragon.impl.event.world.PlayerCollideWithBlockEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketClickWindow
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketHeldItemChange
import net.minecraftforge.client.event.InputUpdateEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * @author Surge
 * @author aesthetical
 */
object NoSlow : Module("NoSlow", Category.MOVEMENT, "Stop certain blocks and actions from slowing you down") {

    private val soulSand = Setting("SoulSand", true) describedBy "Stop soul sand from slowing you down"
    private val slime = Setting("Slime", true) describedBy "Stop slime blocks from slowing you down"
    private val items = Setting("Items", true) describedBy "Stop items from slowing you down"
    private val ncpStrict = Setting("NCPStrict", false) describedBy "If to bypass NCP strict checks"

    private var sneakState = false
    private var sprintState = false

    override fun onDisable() {
        super.onDisable()
        if (mc.anyNull) {
            return
        }

        if (sneakState && !mc.player.isSneaking) {
            mc.player.connection.sendPacket(
                CPacketEntityAction(
                    mc.player, CPacketEntityAction.Action.STOP_SNEAKING
                )
            )
            sneakState = false
        }

        if (sprintState && mc.player.isSprinting) {
            mc.player.connection.sendPacket(
                CPacketEntityAction(
                    mc.player, CPacketEntityAction.Action.START_SPRINTING
                )
            )
            sprintState = false
        }
    }

    @SubscribeEvent
    fun onInput(event: InputUpdateEvent?) {
        if (mc.anyNull) {
            return
        }

        if (items.value && mc.player.isHandActive && !mc.player.isRiding) {
            mc.player.movementInput.moveForward *= 5
            mc.player.movementInput.moveStrafe *= 5

            if (ncpStrict.value) {
                // funny NCP bypass - good job ncp devs
                mc.player.connection.sendPacket(CPacketHeldItemChange(mc.player.inventory.currentItem))
            }
        }
    }

    @Listener
    fun onCollideWithBlock(event: PlayerCollideWithBlockEvent) {
        if (event.blockType === Blocks.SOUL_SAND && soulSand.value || event.blockType === Blocks.SLIME_BLOCK && slime.value) {
            event.cancel()
        }
    }

    @Listener
    fun onPacketSendPre(event: PreSend) {
        if (event.packet is CPacketClickWindow && ncpStrict.value) {

            // i love ncp updated devs - the inventory checks are almost as good as verus's
            if (!mc.player.isSneaking) {
                sneakState = true
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player, CPacketEntityAction.Action.START_SNEAKING
                    )
                )
            }

            if (mc.player.isSprinting) {
                sprintState = true
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player, CPacketEntityAction.Action.STOP_SPRINTING
                    )
                )
            }
        }
    }

    @Listener
    fun onPacketSendPost(event: PostSend) {
        if (event.packet is CPacketClickWindow && ncpStrict.value) {

            // reset states
            if (sneakState && !mc.player.isSneaking) {
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player, CPacketEntityAction.Action.STOP_SNEAKING
                    )
                )
                sneakState = false
            }

            if (sprintState && mc.player.isSprinting) {
                mc.player.connection.sendPacket(
                    CPacketEntityAction(
                        mc.player, CPacketEntityAction.Action.START_SPRINTING
                    )
                )
                sprintState = false
            }
        }
    }

}