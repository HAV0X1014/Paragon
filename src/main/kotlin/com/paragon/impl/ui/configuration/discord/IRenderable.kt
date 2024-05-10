package com.paragon.impl.ui.configuration.discord

/**
 * @author SooStrator1136
 */
interface IRenderable {

    fun render(mouseX: Int, mouseY: Int)
    fun onClick(mouseX: Int, mouseY: Int, button: Int)
    fun onRelease(mouseX: Int, mouseY: Int, button: Int)
    fun onKey(keyCode: Int)

}