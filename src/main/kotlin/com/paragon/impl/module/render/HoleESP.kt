package com.paragon.impl.module.render

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import com.paragon.util.system.backgroundThread
import com.paragon.util.world.BlockUtil
import com.paragon.util.world.BlockUtil.getBlockAtPos
import com.paragon.util.world.BlockUtil.getSurroundingBlocks
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier
import kotlin.math.floor

/**
 * @author Surge
 * @since 19/10/2022
 */
object HoleESP : Module("HoleESP", Category.RENDER, "Highlights holes to stand in when crystalling") {

    // Hole filters and colours
    private val obsidian = Setting("Obsidian", true) describedBy "Highlight obsidian holes"
    private val obsidianColour = Setting("Colour", Color.RED.integrateAlpha(130f)) describedBy "Colour of obsidian holes" subOf obsidian
    private val mixed = Setting("Mixed", true) describedBy "Highlight mixed holes (holes that are a mix of obsidian and bedrock)"
    private val mixedColour = Setting("Colour", Color.ORANGE.integrateAlpha(130f)) describedBy "The colour for mixed holes" subOf (mixed)
    private val bedrock = Setting("Bedrock", true) describedBy "Highlight bedrock holes"
    private val bedrockColour = Setting("Colour", Color.GREEN.integrateAlpha(130f)) describedBy "The colour for bedrock holes" subOf bedrock

    private val range = Setting("Range", 5f, 2f, 20f, 1f) describedBy "The range to search for holes"

    // Render settings
    private val fill = Setting("Fill", true) describedBy "Fill the holes ;)"
    private val fillHeight = Setting("Height", 0f, 0f, 1.5f, 0.01f) describedBy "How tall the fill is" subOf fill

    private val outline = Setting("Outline", true) describedBy "Outline the hole"
    private val outlineWidth = Setting("Width", 1f, 1f, 3f, 0.01f) describedBy "The width of the outlines" subOf outline
    private val outlineHeight = Setting("Height", 0f, 0f, 1.5f, 0.01f) describedBy "How tall the outline is" subOf outline

    private val glow = Setting("Glow", true) describedBy "Renders a glow effect in the box"
    private val glowHeight = Setting("Height", 1f, 0f, 1.5f, 0.01f) describedBy "How tall the glow is" subOf glow

    private val gradientOutline = Setting("GradientOutline", true) describedBy "Renders a gradient outline effect above the box"
    private val gradientOutlineHeight = Setting("Height", 1f, 0f, 1.5f, 0.01f) describedBy "How tall the gradient outline is" subOf gradientOutline

    private val hideCurrent = Setting("Hide Current", false) describedBy "Doesn't render the hole if you are standing in it"

    private val threaded = Setting("Threaded", false) describedBy "Use threads when finding holes"

    // List of holes to render
    private val holes = ConcurrentHashMap<BlockPos, HoleType>()

    // the last job that was run
    private var lastJob: Job? = null

    override fun onDisable() {
        // clear if we have disabled the module
        holes.clear()
    }

    override fun onTick() {
        if (mc.anyNull) {
            // clear if we aren't in a world
            holes.clear()

            return
        }

        if (threaded.value) {
            // launch new job in background thread
            backgroundThread {
                if (lastJob == null || (lastJob ?: return@backgroundThread).isCompleted) {
                    lastJob = launch {
                        // find the valid holes
                        findHoles()
                    }
                }
            }
        }

        else {
            // find holes in main thread
            findHoles()
        }

        holes.entries.removeIf { (pos, _) ->
            pos.getDistance(
                mc.player.posX.toInt(),
                mc.player.posY.toInt(),
                mc.player.posZ.toInt()
            ) > range.value || hideCurrent.value && pos.x == floor(mc.player.posX).toInt() && pos.y == floor(
                mc.player.posY
            ).toInt() && pos.z == floor(mc.player.posZ).toInt() || getHoleType(pos) == null
        }
    }

    override fun onRender3D() {
        holes.forEach { (pos, type) ->
            // get AABB of the position
            val blockBB = BlockUtil.getBlockBox(pos)

            // render fill
            if (fill.value) {
                RenderBuilder()
                    .boundingBox(AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + fillHeight.value, blockBB.maxZ))
                    .inner(type.colour.get())
                    .type(BoxRenderMode.FILL)
                    .start()
                    .blend(true)
                    .depth(true)
                    .texture(true)
                    .build(false)
            }

            // render outline
            if (outline.value) {
                RenderBuilder()
                    .boundingBox(AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + outlineHeight.value, blockBB.maxZ))
                    .outer(type.colour.get().integrateAlpha(255f))
                    .type(BoxRenderMode.OUTLINE)
                    .start()
                    .blend(true).depth(true).texture(true).lineWidth(outlineWidth.value)
                    .build(false)
            }

            // render glow
            if (glow.value) {
                RenderUtil.drawGradientBox(
                    AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + glowHeight.value, blockBB.maxZ),
                    Color(0, 0, 0, 0),
                    type.colour.get()
                )
            }

            // render gradient outline
            if (gradientOutline.value) {
                RenderUtil.drawOutlineGradientBox(
                    AxisAlignedBB(blockBB.minX, blockBB.minY, blockBB.minZ, blockBB.maxX, blockBB.minY + gradientOutlineHeight.value, blockBB.maxZ),
                    Color(0, 0, 0, 0),
                    type.colour.get().integrateAlpha(255f)
                )
            }
        }
    }

    private fun findHoles() {
        BlockUtil.getSphere(range.value, false).forEach {
            // we get the hole type. if it is null, we continue onto the next element in the list
            holes[it] = getHoleType(it) ?: return@forEach
        }
    }

    private fun getHoleType(pos: BlockPos): HoleType? {
        // Return null (invalid hole) if the given [pos] isn't air or the block below it is replaceable
        if (!pos.getBlockAtPos().isReplaceable(mc.world, pos) || pos.down().getBlockAtPos().isReplaceable(mc.world, pos.down())) {
            return null
        }

        // get the surrounding blocks of the given [pos]
        val surrounding = pos.getSurroundingBlocks()

        // get hole type
        if (surrounding.all { it == Blocks.OBSIDIAN }) {
            return HoleType.OBSIDIAN
        }

        else if (surrounding.all { it == Blocks.BEDROCK }) {
            return HoleType.BEDROCK
        }

        else if (surrounding.all { it == Blocks.OBSIDIAN || it == Blocks.BEDROCK }) {
            return HoleType.MIXED
        }

        // else we will return invalid hole
        return null
    }

    enum class HoleType(val colour: Supplier<Color>) {
        /**
         * A hole made of obsidian
         */
        OBSIDIAN({ obsidianColour.value }),

        /**
         * A hole made of bedrock or obsidian
         */
        MIXED({ mixedColour.value }),

        /**
         * A hole made of bedrock
         */
        BEDROCK({ bedrockColour.value })
    }

}