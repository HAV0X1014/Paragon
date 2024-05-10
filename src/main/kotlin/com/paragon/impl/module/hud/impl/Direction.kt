package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.player.PlayerUtil
import com.paragon.util.string.StringUtil
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Direction : TextHUDModule(
    "Direction",
    "Draws the direction you are currently facing in to the screen",
    { "Direction ${TextFormatting.GRAY}[${TextFormatting.WHITE}${StringUtil.getFormattedText(PlayerUtil.direction)}, ${PlayerUtil.getAxis(PlayerUtil.direction)}${TextFormatting.GRAY}]" }
)