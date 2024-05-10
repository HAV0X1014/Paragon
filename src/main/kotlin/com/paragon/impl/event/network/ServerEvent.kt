package com.paragon.impl.event.network

import com.paragon.bus.event.CancellableEvent

open class ServerEvent : CancellableEvent() {

    class Connect(val state: State) : ServerEvent()

    enum class State {

        /**
         * Called before the connection attempt
         */
        PRE,

        /**
         * Indicates that the connection attempt was successful
         */
        CONNECT,

        /**
         * Indicates that an exception occurred when trying to connect to the target server.
         * This will be followed by an instance of [ServerEvent.Disconnect] being posted.
         */
        FAILED
    }

    class Disconnect(forced: Boolean) : ServerEvent() {

        /**
         * @return Whether the connection was forcefully closed
         */
        val isForced: Boolean = forced

    }

}