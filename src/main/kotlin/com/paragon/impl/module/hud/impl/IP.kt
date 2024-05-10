package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object IP : TextHUDModule(
    "IP",
    "Draws the IP of the current server you are on to the screen",
    { "IP ${TextFormatting.GRAY}[${TextFormatting.WHITE}${ if (mc.isSingleplayer) "Singleplayer" else mc.currentServerData?.serverIP }${TextFormatting.GRAY}]" }
)