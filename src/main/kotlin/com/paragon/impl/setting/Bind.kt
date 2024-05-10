package com.paragon.impl.setting

import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

/**
 * @author Surge
 * @author SooStrator1136
 */
class Bind(var buttonCode: Int, var device: Device) {

    private var alreadyPressed = false

    fun isPressed(): Boolean {
        if (buttonCode == 0) {
            return false
        }

        return if (Keyboard.isKeyDown(buttonCode) && device == Device.KEYBOARD || Mouse.isButtonDown(buttonCode) && device == Device.MOUSE) {
            // We haven't already pressed the key
            if (!alreadyPressed) {
                alreadyPressed = true
                true
            } else {
                false
            }
        } else {
            alreadyPressed = false
            false
        }
    }

    fun getButtonName(): String = if (device == Device.KEYBOARD) {
        Keyboard.getKeyName(buttonCode)
    } else Mouse.getButtonName(buttonCode)


    enum class Device {
        /**
         * A key on the keyboard
         */
        KEYBOARD,

        /**
         * A mouse button
         */
        MOUSE
    }

}