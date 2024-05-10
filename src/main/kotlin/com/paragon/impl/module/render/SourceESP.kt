package com.paragon.impl.module.render

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.system.backgroundThread
import com.paragon.util.world.BlockUtil
import com.paragon.util.world.BlockUtil.getBlockAtPos
import com.paragon.util.world.BlockUtil.isSource
import kotlinx.coroutines.Job
import net.minecraft.block.BlockLiquid
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object SourceESP : Module("SourceESP", Category.RENDER, "Highlights liquid source blocks") {

    private val range = Setting(
        "Range", 20F, 5F, 50F, 1F
    ) describedBy "Range to search in"

    private val onlyTop = Setting(
        "OnlyTop", true
    )

    private val lavaColor = Setting(
        "Lava color", Color.RED.integrateAlpha(75F)
    ) describedBy "Color with which lava will be highlighted"
    private val waterColor = Setting(
        "Water color", Color.BLUE.integrateAlpha(75F)
    ) describedBy "Color with which water will be highlighted"

    //Might fuck up performance 🤷
    private val sources: MutableList<BlockPos> = CopyOnWriteArrayList()

    private var lastJob: Job? = null

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        if (lastJob == null || (lastJob ?: return).isCompleted) {
            lastJob = backgroundThread {
                sources.addAll(BlockUtil.getSphere(range.value, true).filter {
                    !sources.contains(it) && it.isSource
                })

                sources.removeIf {
                    if (onlyTop.value && !isTopSource(it)) {
                        return@removeIf true
                    }

                    return@removeIf !it.isSource || it.getDistance(
                        mc.player.posX.toInt(), mc.player.posY.toInt(), mc.player.posZ.toInt()
                    ) > range.value
                }
            }
        }
    }

    private fun isTopSource(pos: BlockPos) = pos.up().getBlockAtPos() !is BlockLiquid

    override fun onRender3D() {
        sources.forEach {
            RenderBuilder().boundingBox(BlockUtil.getBlockBox(it)).inner(if (it.getBlockAtPos() == Blocks.WATER) waterColor.value else lavaColor.value).outer(
                    if (it.getBlockAtPos() == Blocks.WATER) {
                        waterColor.value.integrateAlpha(255f)
                    }
                    else {
                        lavaColor.value.integrateAlpha(255f)
                    }
                ).type(BoxRenderMode.BOTH)

                .start()

                .blend(true).depth(true).texture(true).lineWidth(1f)

                .build(false)
        }
    }

    override fun onDisable() {
        lastJob = null
        sources.clear()
    }

}