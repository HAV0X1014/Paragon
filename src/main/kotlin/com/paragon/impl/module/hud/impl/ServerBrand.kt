package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.mc
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object ServerBrand : TextHUDModule(
    "ServerBrand",
    "Draws the current server's type to the screen",
    { "ServerBrand ${TextFormatting.GRAY}[${TextFormatting.WHITE}${if (mc.player != null) if (mc.isSingleplayer) "Singleplayer" else mc.player.serverBrand else ""}${TextFormatting.GRAY}]" }
)