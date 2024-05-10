package com.paragon.util.system

import com.paragon.util.mc
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.ITextureObject
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.util.ResourceLocation
import org.apache.commons.io.FilenameUtils
import org.lwjgl.opengl.GL11.*
import javax.imageio.ImageIO

/**
 * @author SooStrator1136
 */
object TextureUtil {

    val client = HttpClient(CIO)

    inline fun getFromURL(url: String, crossinline exceptionHandler: (Throwable) -> Unit): ResourceLocation {
        val resourceLoc = ResourceLocation("downloaded/${FilenameUtils.getBaseName(url)}")
        val textureObj: ITextureObject? = mc.textureManager.getTexture(resourceLoc)

        if (textureObj == null || textureObj == TextureUtil.MISSING_TEXTURE) {
            mc.textureManager.loadTexture(resourceLoc, runBlocking {
                runCatching {
                    DynamicTexture(withContext(Dispatchers.IO) {
                        ImageIO.read(client.get(url).readBytes().inputStream())
                    })
                }.onFailure {
                    exceptionHandler(it)
                }.getOrElse { TextureUtil.MISSING_TEXTURE }
            })
        }

        return resourceLoc
    }

    fun getTextureBounds(texture: ResourceLocation): Pair<Int, Int> {
        mc.textureManager.bindTexture(texture)
        return Pair(
            glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH),
            glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)
        )
    }

}