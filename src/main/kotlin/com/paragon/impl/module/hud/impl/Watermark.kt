package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.hud.TextHUDModule
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object Watermark : TextHUDModule(
    "Watermark",
    "Draws the client's name and version to the screen",
    { "${Paragon.NAME} ${TextFormatting.WHITE}${Paragon.VERSION}" }
)