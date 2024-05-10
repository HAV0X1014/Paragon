package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Hunger : TextHUDModule(
    "Hunger",
    "Draws your current hunger level to the screen",
    { "Hunger ${TextFormatting.GRAY}[${TextFormatting.WHITE}${mc.player.foodStats.foodLevel}${TextFormatting.GRAY}]" }
)