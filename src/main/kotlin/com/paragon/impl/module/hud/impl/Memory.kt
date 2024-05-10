package com.paragon.impl.module.hud.impl

import com.paragon.impl.module.hud.TextHUDModule
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Memory : TextHUDModule(
    "Memory",
    "Draws the current amount of memory being used to the screen",
    {
        "Memory ${TextFormatting.GRAY}[${TextFormatting.WHITE}${
            String.format("Memory: % 2d%% %03d/%03dMB",
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100L / Runtime.getRuntime().maxMemory(),
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L, 
                Runtime.getRuntime().maxMemory() / 1024L / 1024L
        ) }${TextFormatting.GRAY}]"
    }
)