package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.ArgumentData
import com.paragon.impl.command.syntax.SyntaxBuilder
import com.paragon.util.system.TextureUtil
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting

/**
 * @author SooStrator1136
 */
object CopySkinCommand : Command("CopySkin", SyntaxBuilder.createBuilder(arrayListOf(
    ArgumentData("name", arrayOf("any_str"))
))
) {

    var skin: ResourceLocation? = null

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        var shouldSet = true

        val newSkin = TextureUtil.getFromURL("https://minotar.net/skin/${args[0]}.png") {
            Paragon.INSTANCE.commandManager.sendClientMessage("${TextFormatting.RED}Couldn't load skin!")
            shouldSet = false // Cross-inlined lambda, can't return
        }

        if (shouldSet) {
            skin = newSkin
            return true
        }

        return false
    }

}