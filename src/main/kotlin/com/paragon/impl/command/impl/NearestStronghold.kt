package com.paragon.impl.command.impl

import com.paragon.impl.command.Command
import com.paragon.impl.command.syntax.SyntaxBuilder
import com.paragon.util.mc
import net.minecraft.util.text.TextFormatting

/**
 * @author EBS
 */
object NearestStronghold : Command("Nearest", SyntaxBuilder()) {

    private val endPortalCoords = arrayOf(
        intArrayOf(1888, -32),
        intArrayOf(-560, 1504),
        intArrayOf(2064, -4400),
        intArrayOf(-4992, -512),
        intArrayOf(2960, 4208),
        intArrayOf(-3200, 4480),
        intArrayOf(-5568, 608),
        intArrayOf(-2496, 5296)
    )

    override fun call(args: Array<String>, fromConsole: Boolean): Boolean {
        // check if server is 2b2t.org using Minecraft.getCurrentServerData()
        if ((mc.currentServerData ?: run {
                sendMessage("${TextFormatting.RED}You need to be on 2b2t.org for this command to work!")
                return false
            }).serverIP == "connect.2b2t.org") {

            // Check if player is in the nether
            if (mc.player.dimension == 1) {
                sendMessage("don't you feel stupid... don't you feel a little ashamed...")
            }

            // get stronghold location nearest to player on 2b2t.org
            var closestX = endPortalCoords[0][0]
            var closestZ = endPortalCoords[0][1]

            var shortestDistance = mc.player.getDistanceSq(
                endPortalCoords[0][0].toDouble(), 0.0, endPortalCoords[0][1].toDouble()
            )

            for (i in 1 until endPortalCoords.size) {
                val distance = mc.player.getDistanceSq(
                    endPortalCoords[i][0].toDouble(), 0.0, endPortalCoords[i][1].toDouble()
                )

                if (distance < shortestDistance) {
                    closestX = endPortalCoords[i][0]
                    closestZ = endPortalCoords[i][1]
                    shortestDistance = distance
                }
            }

            sendMessage("Nearest stronghold around ($closestX, $closestZ)")

            return true
        } else {
            return false
        }
    }
}