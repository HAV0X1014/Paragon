package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object FPS : TextHUDModule(
    "FPS",
    "Draws the current frames per second to the screen",
    { "FPS ${TextFormatting.GRAY}[${TextFormatting.WHITE}${Minecraft.getDebugFPS()}${TextFormatting.GRAY}]" }
)