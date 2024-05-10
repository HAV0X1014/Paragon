package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.SyntaxBuilder

/**
 * @author Surge
 */
object HelpCommand : Command("Help", SyntaxBuilder()) {

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        Paragon.INSTANCE.commandManager.commands.forEach {
            sendMessage(it.name)
        }

        return true
    }

}