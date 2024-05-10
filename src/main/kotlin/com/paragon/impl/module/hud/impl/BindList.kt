package com.paragon.impl.module.hud.impl

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.hud.TextHUDModule
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 * @since 24/12/2022
 */
object BindList : TextHUDModule(
    "BindList",
    "Draws a list of module binds to the screen",
    { BindList.generate() }
) {
    fun generate(): String {
        var text = ""

        Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.bind.value.buttonCode != 0 && it.category != Category.CLIENT && it.category != Category.HUD }.forEach {
            text += "${TextFormatting.RESET}${it.name} ${TextFormatting.GRAY}[${TextFormatting.WHITE}${it.bind.value.getButtonName()}${TextFormatting.GRAY}]${System.lineSeparator()}"
        }

        return text
    }
}