package com.paragon.impl.managers

import com.paragon.impl.module.Module
import com.paragon.impl.module.client.*
import com.paragon.impl.module.combat.*
import com.paragon.impl.module.hud.impl.*
import com.paragon.impl.module.hud.impl.graph.GraphCrystals
import com.paragon.impl.module.hud.impl.graph.GraphFPS
import com.paragon.impl.module.hud.impl.graph.GraphPing
import com.paragon.impl.module.hud.impl.graph.GraphSpeed
import com.paragon.impl.module.misc.*
import com.paragon.impl.module.movement.*
import com.paragon.impl.module.render.*
import com.paragon.util.hasField
import me.soostrator.cti.InheritanceResolver
import net.minecraftforge.common.MinecraftForge
import java.util.function.Predicate

class ModuleManager {

    val modules: MutableList<Module> = InheritanceResolver.resolveAllHeirs(Module::class.java).filter {
        it.hasField("INSTANCE")
    }.map {
        runCatching {
            it.getField("INSTANCE").get(null)
        }.getOrNull() as Module
    }.toMutableList()

    init {
        MinecraftForge.EVENT_BUS.register(this)
        modules.forEach { it.reflectSettings() }
    }


    /**
     * @return all the modules matching the predicate
     */
    fun getModulesThroughPredicate(predicate: Predicate<Module>): List<Module> = modules.filter { predicate.test(it) }

}