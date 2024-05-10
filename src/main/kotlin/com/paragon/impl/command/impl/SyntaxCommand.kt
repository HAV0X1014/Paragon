package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.ArgumentData
import com.paragon.impl.command.syntax.SyntaxBuilder

/**
 * @author Surge
 */
object SyntaxCommand : Command("Syntax", SyntaxBuilder.createBuilder(arrayListOf(
    ArgumentData("command", arrayOf("any_str"))
))) {

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        if (args.size == 1) {
            for (command in Paragon.INSTANCE.commandManager.commands) {
                if (command.name.equals(args[0], true)) {
                    sendMessage("${command.name} ${command.syntax.join()}")
                    return true
                }
            }
        }

        return false
    }

}