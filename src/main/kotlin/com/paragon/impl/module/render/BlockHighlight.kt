package com.paragon.impl.module.render

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.world.BlockHighlightEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.mc
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.builder.BoxRenderMode
import com.paragon.util.render.builder.RenderBuilder
import net.minecraft.util.math.RayTraceResult
import java.awt.Color

/**
 * @author Surge
 */
object BlockHighlight : Module("BlockHighlight", Category.RENDER, "Highlights the block you are looking at") {

    private val renderMode = Setting(
        "RenderMode", BoxRenderMode.BOTH
    ) describedBy "How to highlight the block"

    private val lineWidth = Setting(
        "LineWidth", 1f, 0.1f, 1.5f, 0.1f
    ) describedBy "The width of the outline" visibleWhen { renderMode.value != BoxRenderMode.FILL }

    private val colour = Setting(
        "Colour", Color(185, 19, 211)
    ) describedBy ("What colour to render the block")

    @Listener
    fun onBlockHighlight(event: BlockHighlightEvent) {
        event.cancel()
    }

    override fun onRender3D() {
        if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
            val bp = mc.objectMouseOver.blockPos

            // Get bounding box (yoinked from RenderGlobal)
            val bb = mc.world.getBlockState(bp)
                .getSelectedBoundingBox(mc.world, bp)
                .grow(0.0020000000949949026)
                .offset(
                    -mc.renderManager.viewerPosX,
                    -mc.renderManager.viewerPosY,
                    -mc.renderManager.viewerPosZ
                )

            RenderBuilder()
                .boundingBox(bb)
                .inner(colour.value)
                .outer(colour.value.integrateAlpha(255f))
                .type(renderMode.value)

                .start()
                .blend(true)
                .depth(true)
                .texture(true)
                .lineWidth(lineWidth.value)
                .build(false)
        }
    }

}