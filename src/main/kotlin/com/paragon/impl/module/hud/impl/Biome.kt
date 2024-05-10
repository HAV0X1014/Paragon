package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Biome : TextHUDModule(
    "Biome",
    "Draws the current biome you are in to the screen",
    { "Biome ${TextFormatting.GRAY}[${TextFormatting.WHITE}${mc.world.getBiome(mc.player.position).biomeName}${TextFormatting.GRAY}]" }
)