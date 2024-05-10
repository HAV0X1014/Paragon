package com.paragon.util.render.shader.shaders

import com.paragon.util.mc
import com.paragon.util.render.shader.Shader
import org.lwjgl.opengl.GL20.*
import java.awt.Color

class SmokeShader : Shader("/assets/paragon/glsl/shaders/smoke.frag") {

    private var colour = Color(0, 0, 0)

    fun setColour(colour: Color) {
        this.colour = colour
    }

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("resolution")
        setupUniform("time")
        setupUniform("col")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform2f(getUniform("resolution"), 1f / mc.displayWidth, 1f / mc.displayHeight)
        glUniform1f(getUniform("time"), time.toFloat())
        glUniform4f(getUniform("col"), colour.red / 255f, colour.green / 255f, colour.blue / 255f, colour.alpha / 255f)
    }

}