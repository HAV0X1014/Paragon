package com.paragon.impl.command.impl

import com.paragon.Paragon
import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.ArgumentData
import com.paragon.impl.command.syntax.SyntaxBuilder
import net.minecraft.util.text.TextFormatting

/**
 * @author Surge
 */
object FriendCommand : Command("Friend", SyntaxBuilder.createBuilder(arrayListOf(
    ArgumentData("action", arrayOf("add", "remove", "list")),
    ArgumentData("name", arrayOf("any_str"), visibleWhen = arrayOf(
        Pair("action", "add"),
        Pair("action", "remove")
    ))
))) {

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        if (args.size == 1 && args[0].equals("list", ignoreCase = true)) {
            // List all players
            if (Paragon.INSTANCE.friendManager.names.isEmpty()) {
                sendMessage("${TextFormatting.RED}You haven't added anyone to your social list!")
            }

            for (player in Paragon.INSTANCE.friendManager.names) {
                Paragon.INSTANCE.commandManager.sendClientMessage(player)
            }

            return true
        } else if (args.size == 2 && args[0].equals("add", ignoreCase = true)) {
            // Add a player
            runCatching {
                val name = args[1]

                Paragon.INSTANCE.friendManager.addName(name)

                sendMessage("${TextFormatting.GREEN}Added player " + name + " to your friends list!")

                // Save social
                Paragon.INSTANCE.storageManager.saveSocial()
            }

            return true
        } else if (args.size == 2 && args[0].equals("remove", ignoreCase = true)) {
            // Remove a player
            val name = args[1]
            Paragon.INSTANCE.friendManager.removePlayer(name)
            sendMessage("${TextFormatting.GREEN}Removed player $name from your friends list!")

            // Save socials
            Paragon.INSTANCE.storageManager.saveSocial()

            return true
        } else {
            return false
        }
    }

}