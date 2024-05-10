package com.paragon.util.calculations

/**
 * @author Surge
 */
class Timer {

    private var milliseconds = -1L

    fun getTime() = System.currentTimeMillis() - milliseconds

    fun hasMSPassed(time: Double) = System.currentTimeMillis() - milliseconds > time

    fun reset() {
        milliseconds = System.currentTimeMillis()
    }

}