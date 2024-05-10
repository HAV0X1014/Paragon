package com.paragon.impl.command

import com.paragon.Paragon
import com.paragon.impl.command.syntax.SyntaxBuilder
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
abstract class Command(val name: String, val syntax: SyntaxBuilder) {

    /**
     * Invoked on command execution, **only called if it's the correct command, no name validation needed**.
     *
     * @param args the parameter the command was called with.
     * @param fromConsole indicates whether the command was executed from the console or not.
     */
    abstract fun call(args: Array<String>, fromConsole: Boolean): Boolean

    fun sendMessage(message: String) = Paragon.INSTANCE.commandManager.sendClientMessage(message)

    fun sendInvalidSyntaxMessage() {
        sendMessage("${TextFormatting.RED}Invalid syntax! Run '\$syntax $name' to get the correct syntax.")
    }

}