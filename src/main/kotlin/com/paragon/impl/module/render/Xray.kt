package com.paragon.impl.module.render

import com.paragon.impl.event.render.world.*
import com.paragon.impl.module.Module
import com.paragon.bus.listener.Listener
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.Category
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.world.BlockUtil.getBlockAtPos
import net.minecraft.init.Blocks

/**
 * @author Surge
 */
@Aliases(["Wallhack"])
object Xray : Module("Xray", Category.RENDER, "Lets you see ores and liquids through blocks") {

    private val visibleBlocks = arrayOf(
        Blocks.DIAMOND_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.COAL_ORE, Blocks.LAPIS_ORE, Blocks.QUARTZ_ORE, Blocks.EMERALD_ORE, Blocks.REDSTONE_ORE, Blocks.LIT_REDSTONE_ORE, Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST
    )

    override fun onEnable() {
        if (mc.anyNull) {
            return
        }

        mc.renderGlobal.loadRenderers()
    }

    override fun onDisable() {
        if (mc.anyNull) {
            return
        }

        mc.renderGlobal.loadRenderers()
    }

    @Listener
    fun onBlockSetOpaque(event: BlockSetOpaqueEvent) {
        event.cancel()
    }

    @Listener
    fun onFullCubeBlock(event: FullCubeBlockEvent) {
        event.returnValue = visibleBlocks.contains(event.block)
        event.cancel()
    }

    @Listener
    fun onRenderBlockModel(event: RenderBlockModelEvent) {
        event.returnValue = !visibleBlocks.contains(event.pos.getBlockAtPos())
        event.cancel()
    }

    @Listener
    fun onRenderSmooth(event: RenderBlockSmoothEvent) {
        event.returnValue = !visibleBlocks.contains(event.pos.getBlockAtPos())
        event.cancel()
    }

    @Listener
    fun onSideRenderBlock(event: SideRenderBlockEvent) {
        event.returnValue = visibleBlocks.contains(event.pos.getBlockAtPos())
        event.cancel()
    }

}