package com.paragon.impl.module.hud.impl.graph

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.network.PacketEvent
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.anyNull
import com.paragon.util.calculations.Timer
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.network.play.client.CPacketUseEntity

/**
 * @author SooStrator1136
 */
object GraphCrystals : HUDModule("CrystalsGraph", "Graph showing the amount of crystals you attack", { 75f }, { 30f }) {

    private val scale = Setting("Size", 1.0, 0.1, 2.0, 0.1) describedBy "Size of the graph"
    private val updateDelay = Setting("Delay", 250.0, 75.0, 1000.0, 25.0)

    private val background = Setting("Background", Graph.Background.ALL)

    private var graph = Graph("Crystals") { background.value }

    private var attackedCrystals = 0.0
    private var actualACrystals = 0.0

    val timer = Timer()
    val atimer = Timer()

    override fun onEnable() {
        graph = Graph("Crystals") { background.value }
    }

    override fun onDisable() {
        attackedCrystals = 0.0
        actualACrystals = 0.0
        timer.reset()
        atimer.reset()
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

        if (atimer.hasMSPassed(1000.0)) {
            attackedCrystals = actualACrystals
            actualACrystals = 0.0
            atimer.reset()
        }

        if (timer.hasMSPassed(updateDelay.value)) {
            graph.update(attackedCrystals)
            timer.reset()
        }
    }

    @Listener
    fun onPacket(event: PacketEvent.PostSend) {
        if (event.packet is CPacketUseEntity && event.packet.action == CPacketUseEntity.Action.ATTACK &&  event.packet.getEntityFromWorld(mc.world) is EntityEnderCrystal) {
            actualACrystals++
        }
    }

}