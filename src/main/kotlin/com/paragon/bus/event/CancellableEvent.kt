package com.paragon.bus.event

/**
 * A basic cancellable event that extends off of the [Event] class
 *
 * @author Surge
 */
open class CancellableEvent : Event() {

    private var cancelled = false

    fun cancel() {
        cancelled = true
    }

    fun isCancelled() = cancelled

}