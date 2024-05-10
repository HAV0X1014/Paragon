package com.paragon.impl.module.movement

import com.paragon.impl.event.player.PlayerMoveEvent
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.Category
import com.paragon.util.mc
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.world.BlockUtil
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import java.awt.Color

/**
 * @author Surge
 */
object AntiVoid : Module("AntiVoid", Category.MOVEMENT, "Avoids void holes for you") {

    private val mode = Setting(
        "Mode", Mode.MOTION
    ) describedBy "How to prevent falling through void holes"

    private var renderPosition: BlockPos? = null

    @Listener
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (mc.player.posY < 2.1 && mc.world.getBlockState(
                BlockPos(
                    mc.player.posX, 0.0, mc.player.posZ
                )
            ).material.isReplaceable
        ) {
            var y = MathHelper.clamp(mc.player.posY, 0.0, Double.MAX_VALUE)

            while (y > 0.0 && mc.world.getBlockState(
                    BlockPos(
                        mc.player.posX, y, mc.player.posZ
                    )
                ).material.isReplaceable
            ) {
                val pos = BlockPos(mc.player.posX, y, mc.player.posZ)

                // Intercepting block
                if (!mc.world.getBlockState(pos).material.isReplaceable) {
                    return
                }

                y--
            }

            when (mode.value) {
                Mode.MOTION -> {
                    event.y = 0.0624
                    mc.player.setVelocity(0.0, 0.0624, 0.0)
                    renderPosition = BlockPos(mc.player.posX, 0.0, mc.player.posZ)
                }

                Mode.LAGBACK -> mc.player.connection.sendPacket(
                    CPacketPlayer.Position(
                        mc.player.posX, mc.player.posY + 100, mc.player.posZ, mc.player.onGround
                    )
                )
            }
        }
        else {
            renderPosition = null
        }
    }

    override fun onRender3D() {
        if (renderPosition != null) {
            RenderBuilder().boundingBox(BlockUtil.getBlockBox(renderPosition ?: return)).inner(Color(200, 0, 0, 150)).outer(Color(200, 0, 0, 255)).type(BoxRenderMode.BOTH)

                .start()

                .blend(true).depth(true).texture(true).lineWidth(1f)

                .build(false)
        }
    }

    enum class Mode {
        /**
         * Adds a slight velocity onto the player's Y motion, could also cause rubberbands
         */
        MOTION,

        /**
         * Send invalid packet to cause lagback / rubberband
         */
        LAGBACK
    }

}