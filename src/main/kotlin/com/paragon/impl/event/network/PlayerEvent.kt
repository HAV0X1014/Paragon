package com.paragon.impl.event.network

/**
 * @author Surge
 */
open class PlayerEvent(val name: String) {

    /**
     * Fired when a player joins the server.
     */
    class PlayerJoinEvent(name: String) : PlayerEvent(name)

    /**
     * Fired when a player leaves the server.
     */
    class PlayerLeaveEvent(name: String) : PlayerEvent(name)

}