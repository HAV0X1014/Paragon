package com.paragon.impl.module.hud.impl.graph

import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.mc
import com.paragon.util.player.PlayerUtil
import com.paragon.util.render.RenderUtil

/**
 * @author SooStrator1136
 */
object GraphSpeed : HUDModule("SpeedGraph", "Graph showing your speed", { 75f }, { 30f }) {

    private val scale = Setting("Size", 1.0, 0.1, 2.0, 0.1) describedBy "Size of the graph"
    private val background = Setting("Background", Graph.Background.ALL)
    private val unit = Setting("Unit", PlayerUtil.Unit.BPS) describedBy "The units in which to display your speed"

    private var graph = Graph("Speed") { background.value }

    override fun onEnable() {
        graph = Graph("Speed") { background.value }
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

        graph.update(PlayerUtil.getSpeed(unit.value))
    }

}