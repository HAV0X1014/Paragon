package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.impl.command.impl.*
import com.paragon.util.mc
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.text.TextFormatting.*
import net.minecraftforge.client.event.ClientChatEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

/**
 * @author Surge
 */
class CommandManager {

    val prefix = "$"
    var lastCommand = ""

    val commands = arrayListOf(
        ConfigCommand,
        CopySkinCommand,
        FriendCommand,
        HelpCommand,
        NearestStronghold,
        OpenFolderCommand,
        SaveMapCommand,
        SettingCommand,
        SyntaxCommand,
        SizeCommand
    )

    val commonPrefixes = listOf("/", ".", "*", ";", ",") as MutableList<String>

    init {
        MinecraftForge.EVENT_BUS.register(this)
        Paragon.INSTANCE.logger.info("Loaded Command Manager")
    }

    private fun handleCommands(message: String, fromConsole: Boolean) {
        if (message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.isNotEmpty()) {
            var commandFound = false
            val commandName = message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

            for (command in commands) {
                if (command.name.equals(commandName, ignoreCase = true)) {
                    if (!command.call(
                            message
                                .split(" ".toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                                .copyOfRange(1, message.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.size),
                            fromConsole
                        )
                    ) {
                        command.sendInvalidSyntaxMessage()
                    }

                    lastCommand = prefix + message
                    commandFound = true
                    break
                }
            }

            if (!commandFound) {
                sendClientMessage(RED.toString() + "Command not found!")
            }
        }
    }

    /**
     * Sends a client side chat message with client prefix.
     */
    fun sendClientMessage(message: String) = mc.player.sendMessage(
        TextComponentString(LIGHT_PURPLE.toString() + "Paragon " + WHITE + "> " + message)
    )

    @SubscribeEvent
    fun onChatMessage(event: ClientChatEvent) {
        // Check if the message starts with the prefix
        if (event.message.startsWith(prefix)) {
            event.isCanceled = true
            handleCommands(event.message.substring(prefix.length), false)
        }
    }

    fun startsWithPrefix(message: String): Boolean {
        return message.split(" ".toRegex())
            .dropLastWhile { it.isEmpty() }[0]
            .lowercase(Locale.getDefault())
            .startsWith(prefix.lowercase())
                ||
                commonPrefixes.contains(
                    message.split(" ".toRegex())
                        .dropLastWhile { it.isEmpty() }[0]
                        .lowercase(Locale.getDefault())
                )
    }

}