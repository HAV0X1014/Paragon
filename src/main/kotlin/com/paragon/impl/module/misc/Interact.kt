package com.paragon.impl.module.misc

import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.event.player.RaytraceEntityEvent
import com.paragon.impl.event.render.entity.SwingArmEvent
import com.paragon.impl.event.world.LiquidInteractEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.Category
import com.paragon.util.mc
import net.minecraft.init.Items
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemPickaxe
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.server.SPacketAnimation
import net.minecraft.util.EnumFacing

/**
 * @author Surge
 * @since 06/08/2022
 */
@Aliases(["LiquidInteract", "BuildHeight", "NoTrace", "NoEntityTrace", "NoSwing"])
object Interact : Module("Interact", Category.MISC, "Changes the way you interact") {

    private val liquids = Setting(
        "Liquids", false
    ) describedBy "Allows you to interact with liquids like normal blocks"

    private val buildHeight = Setting(
        "BuildHeight", true
    ) describedBy "Lets you place blocks at Y 255"

    private val noTrace = Setting(
        "NoTrace", false
    ) describedBy "Cancel raytracing entities"

    private val pickaxe = Setting(
        "Pickaxe", true
    ) describedBy "Ignores entities when you are holding a pickaxe" subOf noTrace

    private val blocks = Setting(
        "Blocks", false
    ) describedBy "Ignores entities when you are holding blocks" subOf noTrace

    private val crystals = Setting(
        "Crystals", true
    ) describedBy "Ignores entities when you are holding crystals" subOf noTrace

    private val noSwing = Setting(
        "NoSwing", false
    ) describedBy "Cancels the swing animation"

    private val mode = Setting(
        "Mode", Mode.PACKET_CANCEL
    ) describedBy "How to not swing" subOf noSwing

    private val others = Setting(
        "Others", true
    ) describedBy "Whether to cancel other players' animations" subOf noSwing

    @Listener
    fun onLiquidInteract(event: LiquidInteractEvent) {
        if (liquids.value) {
            event.cancel()
        }
    }

    @Listener
    fun onPacketSent(event: PacketEvent.PreSend) {
        // Check packet is a player try use item on block packet
        if (!buildHeight.value || event.packet !is CPacketPlayerTryUseItemOnBlock) {
            return
        }

        // Get packet
        val packet = event.packet

        // Check the position we are trying to place at is 255 or above, and we are placing on top of a block
        if (packet.pos.y >= 255 && packet.direction == EnumFacing.UP) {

            // Send new packet with the place direction being down
            mc.player.connection.sendPacket(
                CPacketPlayerTryUseItemOnBlock(
                    packet.pos, EnumFacing.DOWN, packet.hand, packet.facingX, packet.facingY, packet.facingZ
                )
            )

            // Don't send original packet
            event.cancel()
        }
    }

    @Listener
    fun onRaytrace(event: RaytraceEntityEvent) {
        if (!noTrace.value) {
            return
        }

        // Cancel if we are holding a pickaxe
        if (pickaxe.value && mc.player.heldItemMainhand.item is ItemPickaxe) {
            event.cancel()
        }

        // Cancel if we are holding crystals
        if (crystals.value && mc.player.heldItemMainhand.item == Items.END_CRYSTAL) {
            event.cancel()
        }

        // Cancel if we are holding blocks
        if (blocks.value && (mc.player.heldItemMainhand.item is ItemBlock || mc.player.heldItemOffhand.item is ItemBlock)) {
            event.cancel()
        }
    }

    @Listener
    fun onPacketSend(event: PacketEvent.PreSend) {
        if (noSwing.value && mode.value == Mode.PACKET_CANCEL && event.packet is CPacketAnimation) {
            event.cancel()
        }
    }

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        if (noSwing.value && others.value && event.packet is SPacketAnimation) {
            event.cancel()
        }
    }

    @Listener
    fun onSwingArm(event: SwingArmEvent) {
        if (noSwing.value && mode.value == Mode.METHOD_CANCEL) {
            event.cancel()
        }
    }

    enum class Mode {
        /**
         * Cancels the swing animation packet
         */
        PACKET_CANCEL,

        /**
         * Cancels the swing method from invoking
         */
        METHOD_CANCEL
    }

}