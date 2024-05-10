package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.ArgumentData
import com.paragon.impl.command.syntax.SyntaxBuilder
import net.minecraft.util.text.TextFormatting
import java.io.File

/**
 * @author Surge
 */
object ConfigCommand : Command("Config", SyntaxBuilder.createBuilder(arrayListOf(
    ArgumentData("action", arrayOf("save", "load", "delete")),
    ArgumentData("name", arrayOf("any_str"))
))) {

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        if (args.size == 2) {
            when (args[0].lowercase()) {
                "save" -> {
                    Paragon.INSTANCE.storageManager.saveModules(args[1])
                    sendMessage("${TextFormatting.GREEN}Config saved successfully!")
                }

                "load" -> {
                    Paragon.INSTANCE.storageManager.loadModules(args[1])
                    sendMessage("${TextFormatting.GREEN}Config loaded successfully!")
                }

                "delete" -> {
                    val file = File("paragon${File.separator}configs${File.separator}${args[1]}")

                    if (file.exists()) {
                        file.delete()
                    } else {
                        sendMessage("${TextFormatting.RED}Could not delete ${args[1]}, because it doesn't exist.")
                    }
                }
            }

            return true
        } else {
            return false
        }
    }

}