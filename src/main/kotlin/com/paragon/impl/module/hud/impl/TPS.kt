package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.hud.TextHUDModule
import com.paragon.util.system.CircularArray
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object TPS : TextHUDModule(
    "TPS",
    "Draws the current server's TPS to the screen",
    { "TPS ${TextFormatting.GRAY}[${TextFormatting.WHITE}${TPS.tpsBuffer.average()}${TextFormatting.GRAY}]" }
) {
    private val tpsBuffer = CircularArray.create(20, 20f)

    override fun onTick() {
        tpsBuffer.add(Paragon.INSTANCE.tpsManager.averageTick)
    }
}