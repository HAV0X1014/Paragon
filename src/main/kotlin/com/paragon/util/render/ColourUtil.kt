package com.paragon.util.render

import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

object ColourUtil {

    /**
     * Creates a rainbow wave
     *
     * @param time How long for each wave
     * @param saturation The saturation of the colour
     * @param addition How much hue to add to the wave
     * @return A rainbow in the RGB format
     */
    fun getRainbow(time: Float, saturation: Float, addition: Int): Int {
        val timeFac = if (time.isNaN()) 0.0 else time * 1000.0
        val hue = (System.currentTimeMillis() + addition) % (time * 1000).toInt() / if (timeFac == 0.0) 1.0 else timeFac
        return Color.HSBtoRGB(hue.toFloat(), saturation, 1f)
    }

    /**
     * Sets the GL colour based on a hex integer
     *
     * @param colourHex The integer of the hex value
     */
    @JvmStatic
    fun setColour(colourHex: Int) {
        glColor4f(
            (colourHex shr 16 and 0xFF) / 255F, (colourHex shr 8 and 0xFF) / 255F, (colourHex and 0xFF) / 255F, (colourHex shr 24 and 0xFF) / 255F
        )
    }

    /**
     * Integrates alpha into a colour
     *
     * @param alpha  The new alpha
     * @return The new colour
     */
    @JvmStatic
    fun Color.integrateAlpha(alpha: Float): Color {
        return Color(
            this.red / 255F, this.green / 255F, this.blue / 255F, alpha.coerceIn(0f, 255f) / 255F
        )
    }

    /**
     * Stolen from Monsoon 3.0 lel
     * (im dev so its fine (probably))
     */
    @JvmStatic
    fun Color.fade(secondary: Color, factor: Double): Color {
        return Color(
            (this.red + (secondary.red - this.red) * factor.coerceIn(0.0, 1.0)).toInt(),
            (this.green + (secondary.green - this.green) * factor.coerceIn(0.0, 1.0)).toInt(),
            (this.blue + (secondary.blue - this.blue) * factor.coerceIn(0.0, 1.0)).toInt(),
            (this.alpha + (secondary.alpha - this.alpha) * factor.coerceIn(0.0, 1.0)).toInt()
        )
    }

    fun Color.glColour() {
        glColor4f(this.red / 255f, this.green / 255f, this.blue / 255f, this.alpha / 255f)
    }

    fun Int.toColour(): Color {
        return Color(this)
    }

    val Int.rgba: FloatArray
        get() = floatArrayOf(
            (this shr 16 and 0xFF) / 255F, // Red
            (this shr 8 and 0xFF) / 255F,  // Green
            (this shr 0 and 0xFF) / 255F,  // Blue
            (this shr 24 and 0xff) / 255F  // Alpha
        )

}