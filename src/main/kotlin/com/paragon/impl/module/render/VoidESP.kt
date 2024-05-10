package com.paragon.impl.module.render

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.system.backgroundThread
import com.paragon.util.world.BlockUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author Surge
 */
object VoidESP : Module("VoidESP", Category.RENDER, "Highlights void holes in the world") {

    private val range = Setting(
        "Range", 5f, 0f, 50f, 1f
    ) describedBy "The range to check for holes"

    // Render settings
    private val fill = Setting("Fill", true) describedBy "Fill the holes ;)" //💀

    private val fillHeight = Setting(
        "Height", 0f, 0f, 2f, 0.01f
    ) describedBy "How tall the fill is" subOf fill

    private val outline = Setting("Outline", true) describedBy "Outline the hole"

    private val outlineWidth = Setting(
        "Width", 1f, 1f, 3f, 1f
    ) describedBy "The width of the outlines" subOf outline

    private val outlineHeight = Setting(
        "Height", 0f, 0f, 2f, 0.01f
    ) describedBy "How tall the outline is" subOf outline

    private val glow = Setting(
        "Gradient", true
    ) describedBy "Renders a glow effect above the box"

    private val glowHeight = Setting(
        "Height", 1f, 0f, 2f, 0.01f
    ) describedBy "How tall the glow is" subOf glow

    private val colour = Setting(
        "Colour", Color(200, 0, 0, 150)
    ) describedBy "The highlight colour"

    private val holes: MutableList<BlockPos> = CopyOnWriteArrayList()

    private var lastJob: Job? = null

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        backgroundThread {
            if (lastJob == null || (lastJob ?: return@backgroundThread).isCompleted) {
                lastJob = launch {
                    holes.addAll(BlockUtil.getSphere(range.value, false).filter {
                        it.y == 0 && mc.world.getBlockState(it).material.isReplaceable && !holes.contains(it)
                    })

                    holes.removeIf {
                        it.y != 0 || !mc.world.getBlockState(it).material.isReplaceable
                    }
                }
            }
        }
    }

    override fun onRender3D() {
        holes.forEach {
            val blockBB = BlockUtil.getBlockBox(it)
            if (fill.value) {
                val fillBB = AxisAlignedBB(
                    blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + fillHeight.value, blockBB.maxZ
                )

                RenderBuilder().boundingBox(fillBB).inner(colour.value).type(BoxRenderMode.FILL)

                    .start()

                    .blend(true).depth(true).texture(true)

                    .build(false)
            }

            if (outline.value) {
                val outlineBB = AxisAlignedBB(
                    blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + outlineHeight.value, blockBB.maxZ
                )

                RenderBuilder().boundingBox(outlineBB).outer(colour.value.integrateAlpha(255f)).type(BoxRenderMode.OUTLINE)

                    .start()

                    .blend(true).depth(true).texture(true).lineWidth(outlineWidth.value)

                    .build(false)
            }

            if (glow.value) {
                val glowBB = AxisAlignedBB(
                    blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + glowHeight.value, blockBB.maxZ
                )
                RenderUtil.drawGradientBox(glowBB, Color(0, 0, 0, 0), colour.value)
            }
        }
    }

}