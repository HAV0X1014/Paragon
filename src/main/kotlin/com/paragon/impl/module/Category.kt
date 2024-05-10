package com.paragon.impl.module

import net.minecraft.init.Items
import net.minecraft.item.Item

/**
 * @author Surge, SooStrator1136
 */
enum class Category(val indicator: Item) {

    COMBAT(Items.END_CRYSTAL),

    /**
     * Movement related modules, e.g. Step, ElytraFlight
     */
    MOVEMENT(Items.DIAMOND_BOOTS),

    /**
     * Render related modules, e.g. Tracers, ESP
     */
    RENDER(Items.ENDER_EYE),

    /**
     * Modules that don't belong to a particular category, e.g. ChatModifications, FakePlayer
     */
    MISC(Items.LAVA_BUCKET),

    /**
     * Client related modules, e.g. GUI, Colours
     */
    CLIENT(Items.WRITABLE_BOOK),

    /**
     * HUD modules
     */
    HUD(Items.COMPASS)

}