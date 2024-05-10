package com.paragon.util.render.shader.shaders

import com.paragon.util.render.shader.Shader
import org.lwjgl.opengl.GL20.*
import java.awt.Color

class DiagonalShader : Shader("/assets/paragon/glsl/shaders/diagonal.frag") {
    private var lineColour: Color? = null
    private var lineWidth = 0f
    private var spacing = 0f

    fun setColour(colour: Color?) {
        lineColour = colour
    }

    fun setWidth(width: Float) {
        lineWidth = width
    }

    fun setSpacing(spacing: Float) {
        this.spacing = spacing
    }

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("time")
        setupUniform("colour")
        setupUniform("size")
        setupUniform("spacing")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform1f(getUniform("time"), time.toFloat())
        glUniform4f(getUniform("colour"), lineColour!!.red / 255f, lineColour!!.green / 255f, lineColour!!.blue / 255f, lineColour!!.alpha / 255f)
        glUniform1f(getUniform("size"), lineWidth)
        glUniform1f(getUniform("spacing"), spacing)
    }
}