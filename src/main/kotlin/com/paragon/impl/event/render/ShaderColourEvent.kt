package com.paragon.impl.event.render

import com.paragon.bus.event.CancellableEvent
import net.minecraft.entity.Entity
import java.awt.Color

/**
 * @author Surge
 */
class ShaderColourEvent(var entity: Entity) : CancellableEvent() {

    var colour: Color? = null

}