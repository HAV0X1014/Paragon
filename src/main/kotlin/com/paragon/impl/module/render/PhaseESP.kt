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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object PhaseESP : Module("PhaseESP", Category.RENDER, "Highlights phased players") {

    private val range = Setting(
        "Range", 20F, 5F, 50F, 1F
    ) describedBy "Range in which to look for phasing players"

    private val phaseColor = Setting(
        "Color", Color.ORANGE.integrateAlpha(100F)
    ) describedBy "Color of the phase indicator"

    private val self = Setting(
        "Self", false
    ) describedBy "Highlight yourself when phasing"

    private val phased: MutableList<EntityPlayer> = CopyOnWriteArrayList()

    private var lastJob: Job? = null

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        backgroundThread {
            if (lastJob == null || (lastJob ?: return@backgroundThread).isCompleted) {
                lastJob = launch {
                    phased.addAll(mc.world.playerEntities.filter {
                        if (it.uniqueID == mc.player.uniqueID && !self.value) {
                            return@filter false
                        }

                        !phased.contains(it) && BlockPos(it.posX, it.posY, it.posZ).getBlockAtPos() != Blocks.AIR && it.getDistance(mc.player) <= range.value
                    })

                    phased.removeIf {
                        it.getDistance(mc.player) > range.value || BlockPos(it.posX, it.posY, it.posZ).getBlockAtPos() == Blocks.AIR || it.uniqueID == mc.player.uniqueID && !self.value
                    }
                }
            }
        }
    }

    override fun onRender3D() {
        phased.forEach {
            RenderBuilder().boundingBox(BlockUtil.getBlockBox(BlockPos(it.posX, it.posY, it.posZ))).inner(phaseColor.value).outer(phaseColor.value.integrateAlpha(255f)).type(BoxRenderMode.BOTH)

                .start()

                .blend(true).depth(true).texture(true).lineWidth(1f)

                .build(false)
        }
    }

    override fun onDisable() {
        phased.clear()
    }

}