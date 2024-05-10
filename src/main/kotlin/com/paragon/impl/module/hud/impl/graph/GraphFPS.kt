package com.paragon.impl.module.hud.impl.graph

import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import net.minecraft.client.Minecraft

/**
 * @author SooStrator1136
 */
object GraphFPS : HUDModule("FPSGraph", "Graph showing your FPS", { 75f }, { 30f }) {

    private val scale = Setting("Size", 1.0, 0.1, 2.0, 0.1) describedBy "Size of the graph"

    private val background = Setting("Background", Graph.Background.ALL)

    private var graph = Graph("FPS") { background.value }

    override fun onEnable() {
        graph = Graph("FPS") { background.value }
    }

    override fun draw() {
        graph.bounds.setRect(x, y, 75F, 30F)

        RenderUtil.scaleTo(x, y, 1F, scale.value, scale.value, 1.0) {
            graph.render()
        }
    }

    override fun onTick() {
        if (mc.anyNull) {
            return
        }

        graph.update(Minecraft.getDebugFPS().toDouble())
    }

}