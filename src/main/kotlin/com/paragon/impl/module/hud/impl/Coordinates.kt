package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.calculations.MathsUtil
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Coordinates : TextHUDModule(
    "Coordinates",
    "Draws the player's coordinates to the screen",
    {
        val inNether = mc.world.getBiome(mc.player.position).biomeName.equals("Hell", true)
        val overworld = "${MathsUtil.roundDouble(mc.player.posX, 1)}, ${MathsUtil.roundDouble(mc.player.posY, 1)}, ${MathsUtil.roundDouble(mc.player.posZ, 1)}"
        val nether = if (Coordinates.nether.value) "${MathsUtil.roundDouble(if (inNether) mc.player.posX * 8 else mc.player.posX / 8, 1)}, ${MathsUtil.roundDouble(if (inNether) mc.player.posY * 8 else mc.player.posY / 8, 1)}, ${MathsUtil.roundDouble(if (inNether) mc.player.posZ * 8 else mc.player.posZ / 8, 1)}" else ""

        "XYZ ${TextFormatting.GRAY}(${TextFormatting.WHITE}$overworld${TextFormatting.GRAY})${if (nether.isNotEmpty()) " ${TextFormatting.GRAY}[${TextFormatting.WHITE}$nether${TextFormatting.GRAY}]" else ""}"
    }
) {
    private val nether = Setting("Nether", true) describedBy "Show the coordinates you would be at when you are in the nether, and vice versa"
}