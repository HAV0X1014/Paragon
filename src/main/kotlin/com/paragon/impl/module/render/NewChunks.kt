package com.paragon.impl.module.render

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.util.math.AxisAlignedBB
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 * @since 20/11/2022
 */
object NewChunks : Module("NewChunks", Category.RENDER, "Highlights chunks that have just been generated") {

    private val renderMode = Setting("RenderMode", BoxRenderMode.BOTH) describedBy "How to render the highlight"
    private val lineWidth = Setting("LineWidth", 1.0f, 0.1f, 3f, 0.1f) describedBy "The width of the outline" visibleWhen { renderMode.value != BoxRenderMode.FILL }
    private val colour = Setting("FillColour", Color(185, 19, 255, 130)) describedBy "The colour of the fill"
    private val outlineColour = Setting("OutlineColour", Color(185, 19, 255))

    private val height = Setting("Height", 0.0, 0.0, 256.0, 1.0) describedBy "The height of the box"

    // List of new chunks
    private val chunkList = CopyOnWriteArrayList<Chunk>()

    override fun onDisable() {
        // Clear on disable
        chunkList.clear()
    }

    override fun onTick() {
        if (mc.anyNull) {
            // Clear if we aren't in a world
            chunkList.clear()
        }
    }

    override fun onRender3D() {
        // Render chunks that are within our render distance. If we don't filter this, then we get some weird rendering bugs.
        chunkList.filter { mc.player.getDistance(it.x.toDouble(), mc.player.posY, it.z.toDouble()) < mc.gameSettings.renderDistanceChunks * 16 }.forEach {
            RenderBuilder()
                .boundingBox(AxisAlignedBB(it.x.toDouble(), 0.0, it.z.toDouble(), it.x + 16.0, height.value, it.z.toDouble() + 16.0))
                .inner(colour.value)
                .outer(outlineColour.value)
                .type(renderMode.value)
                .start()
                .lineWidth(lineWidth.value)
                .blend(true)
                .depth(true)
                .texture(true)
                .build(true)
        }
    }

    @Listener
    fun onPacketReceive(event: PacketEvent.PreReceive) {
        if (event.packet is SPacketChunkData) {
            // The chunk hasn't finished generating
            if (!event.packet.isFullChunk) {
                // This chunk isn't in our list
                if (!chunkList.any { it.x == event.packet.chunkX && it.z == event.packet.chunkZ }) {
                    chunkList.add(Chunk(event.packet.chunkX * 16, event.packet.chunkZ * 16))
                }
            }
        }
    }

    data class Chunk(val x: Int, val z: Int)

}