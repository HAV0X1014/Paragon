package com.paragon.impl.module.render

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.entity.EnchantColourEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import java.awt.Color

/**
 * @author Surge
 * @since 19/11/22
 */
object EnchantColour : Module("EnchantColour", Category.RENDER, "Changes the colour of enchants") {

    private val colour = Setting("Colour", Color(185, 17, 255)) describedBy "The colour of the enchant"

    @Listener
    fun onEnchantColour(event: EnchantColourEvent) {
        event.colour = colour.value
    }

}