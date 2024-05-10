package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Ping : TextHUDModule(
    "Ping",
    "Draws the network response time to the screen",
    { "Ping ${TextFormatting.GRAY}[${TextFormatting.WHITE}${mc.connection?.getPlayerInfo(mc.player.gameProfile.id)?.responseTime}${TextFormatting.GRAY}]" }
)