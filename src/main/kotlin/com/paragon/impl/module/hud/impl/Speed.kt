package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.player.PlayerUtil
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Speed : TextHUDModule(
    "Speed",
    "Draws your current player speed to the screen",
    { "Speed ${TextFormatting.GRAY}[${TextFormatting.WHITE}%.2f${TextFormatting.GRAY}".format(PlayerUtil.getSpeed(Speed.unit.value)) + "${TextFormatting.WHITE}${Speed.unit.value.name.lowercase()}]" }
) {
    private val unit = Setting("Unit", PlayerUtil.Unit.BPS) describedBy "The units in which to display your speed"
}