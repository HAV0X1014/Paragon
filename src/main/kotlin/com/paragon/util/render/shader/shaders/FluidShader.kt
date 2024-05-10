package com.paragon.util.render.shader.shaders

import com.paragon.util.render.shader.Shader
import org.lwjgl.opengl.GL20.glUniform1f
import org.lwjgl.opengl.GL20.glUniform1i

class FluidShader : Shader("/assets/paragon/glsl/shaders/fluid.frag") {

    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("time")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform1f(getUniform("time"), time.toFloat())
    }

}