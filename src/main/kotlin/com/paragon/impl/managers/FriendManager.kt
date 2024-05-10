package com.paragon.impl.managers

import net.minecraft.entity.player.EntityPlayer

/**
 * @author Surge
 * @author SooStrator1136
 */
class FriendManager {

    // Wanted to use UUIDs, but Mojang is obese and will rate limit me
    val names: MutableList<String> = ArrayList()

    /**
     * Checks if a given player name is a friend
     *
     * @param name The name to check
     * @return If the given name is a friend
     */
    fun isFriend(name: String) = names.contains(name)

    /**
     * Adds a player to our players list
     *
     * @param name The player name to add
     */
    fun addName(name: String) {
        if (!names.contains(name)) {
            names.add(name)
        }
    }

    /**
     * Adds a player to our players list
     *
     * @param player The player to add
     */
    fun addPlayer(player: EntityPlayer) {
        if (!names.contains(player.name)) {
            names.add(player.name)
        }
    }

    /**
     * Removes a player's name from our name list
     *
     * @param name The name of the player to remove
     */
    fun removePlayer(name: String) = names.removeIf { it == name }.let { return@let }

}