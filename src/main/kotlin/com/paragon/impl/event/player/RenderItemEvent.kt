package com.paragon.impl.event.player

import com.paragon.bus.event.Event
import net.minecraft.util.EnumHandSide

/**
 * @author Surge
 */
open class RenderItemEvent(val side: EnumHandSide) : Event() {

    /**
     * Fired before an item is rendered.
     */
    class Pre(enumHandSide: EnumHandSide) : RenderItemEvent(enumHandSide)

    /**
     * Fired after an item is rendered.
     */
    class Post(enumHandSide: EnumHandSide) : RenderItemEvent(enumHandSide)

}