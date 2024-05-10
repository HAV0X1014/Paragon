package com.paragon.util.render.shader.shaders

import com.paragon.util.mc
import com.paragon.util.render.shader.Shader
import org.lwjgl.opengl.GL20.*
import java.awt.Color

class OutlineShader : Shader("/assets/paragon/glsl/shaders/outline.frag") {

    private var colour = Color(0, 0, 0)
    private var width = 1f
    private var fill = 0
    private var outline = 1

    fun setColour(colour: Color) {
        this.colour = colour
    }

    fun setWidth(width: Float) {
        this.width = width
    }

    fun setFill(fill: Int) {
        this.fill = fill
    }

    fun setOutline(outline: Int) {
        this.outline = outline
    }

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("resolution")
        setupUniform("colour")
        setupUniform("width")
        setupUniform("fill")
        setupUniform("outline")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform2f(getUniform("resolution"), 1f / mc.displayWidth, 1f / mc.displayHeight)
        glUniform4f(getUniform("colour"), colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)
        glUniform1f(getUniform("width"), width)
        glUniform1i(getUniform("fill"), fill)
        glUniform1i(getUniform("outline"), outline)
    }

}