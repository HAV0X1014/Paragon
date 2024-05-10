package com.paragon.util.render.shader

import com.paragon.Paragon
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.ARBShaderObjects
import org.lwjgl.opengl.GL20.*
import java.nio.charset.Charset

/**
 * @author Surge, Cosmos
 */
open class Shader(path: String) {

    private var program = 0
    private var uniforms: MutableMap<String, Int>? = null
    var time = 0.0

    init {
        var vertex = 0
        var fragment = 0

        try {
            val vertStream = javaClass.getResourceAsStream("/assets/paragon/glsl/vertex.vert")

            if (vertStream != null) {
                vertex = createShader(IOUtils.toString(vertStream, Charset.defaultCharset()), GL_VERTEX_SHADER)
                IOUtils.closeQuietly(vertStream)
                Paragon.INSTANCE.logger.info("{} - Vertex shader loaded", path)
            }

            val fragStream = javaClass.getResourceAsStream(path)

            if (fragStream != null) {
                fragment = createShader(IOUtils.toString(fragStream, Charset.defaultCharset()), GL_FRAGMENT_SHADER)
                IOUtils.closeQuietly(fragStream)
                Paragon.INSTANCE.logger.info("{} - Fragment shader loaded", path)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (vertex != 0 && fragment != 0) {
            program = ARBShaderObjects.glCreateProgramObjectARB()

            if (program != 0) {
                ARBShaderObjects.glAttachObjectARB(program, vertex)
                ARBShaderObjects.glAttachObjectARB(program, fragment)
                ARBShaderObjects.glLinkProgramARB(program)
                ARBShaderObjects.glValidateProgramARB(program)
                Paragon.INSTANCE.logger.info("{} - Shader program loaded", path)
            }
        }
    }

    fun startShader() {
        glUseProgram(program)

        if (uniforms == null) {
            uniforms = HashMap()
            setupUniforms()
        }

        updateUniforms()
    }

    open fun setupUniforms() {}
    open fun updateUniforms() {}

    private fun createShader(source: String, type: Int): Int {
        var shader = 0
        return try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(type)

            if (shader != 0) {
                ARBShaderObjects.glShaderSourceARB(shader, source)
                ARBShaderObjects.glCompileShaderARB(shader)

                if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0) {
                    throw RuntimeException(
                        "Error creating shader: " + ARBShaderObjects.glGetInfoLogARB(
                            shader,
                            ARBShaderObjects.glGetObjectParameteriARB(shader, 35716)
                        )
                    )
                }
                shader
            } else {
                0
            }
        } catch (e: Exception) {
            ARBShaderObjects.glDeleteObjectARB(shader)
            e.printStackTrace()
            throw e
        }
    }

    fun setupUniform(name: String) {
        (uniforms ?: return)[name] = glGetUniformLocation(program, name)
    }

    fun getUniform(name: String): Int {
        return uniforms!![name]!!
    }
}