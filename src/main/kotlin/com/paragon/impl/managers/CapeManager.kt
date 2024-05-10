package com.paragon.impl.managers

import com.paragon.Paragon
import com.paragon.util.mc
import com.paragon.util.system.TextureUtil
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.minecraftforge.fml.relauncher.FMLLaunchHandler
import java.util.*

class CapeManager {

    private val capedPlayers: MutableMap<String, Cape> = HashMap()

    /**
     * Checks if a player has a cape using the players username
     */
    fun isCaped(username: String) = capedPlayers.containsKey(username)

    /**
     * Gets the cape for a given [username]
     */
    fun getCape(username: String) = capedPlayers[username]

    init {
        runCatching {
            runBlocking {
                TextureUtil.client.get("https://ParagonBot.wolfsurge.repl.co/capes").bodyAsText().split(',').forEach {
                    val data = it.split(":")
                    capedPlayers[data[0]] = Cape.valueOf(data[1].uppercase(Locale.getDefault()))
                }
            }
        }.onSuccess {
            Paragon.INSTANCE.logger.info("Loaded capes!")
        }.onFailure {
            Paragon.INSTANCE.logger.error("Couldn't fetch capes! Looks like the host is down.")
            it.printStackTrace()
        }

        // Give a cape to the player if we are in a dev env
        if (FMLLaunchHandler.isDeobfuscatedEnvironment()) {
            capedPlayers[mc.session.username] = Cape.BASED
        }
    }

    enum class Cape(val path: String) {
        /**
         * Cape for cool ppl (boosters etc.)
         */
        COOL("textures/cape/cool.png"),

        /**
         * Cape for based ppl (contributors etc.)
         */
        BASED("textures/cape/based.png")
    }

}