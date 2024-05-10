package com.paragon.phobosgui.simple.element.setting.impl

import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.phobos.element.setting.SettingElement
import com.paragon.impl.ui.configuration.phobos.element.setting.impl.BooleanElement
import com.paragon.impl.ui.configuration.phobos.element.setting.impl.SliderElement
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

/**
 * @author Surge
 * @since 31/07/2022
 */
class ColourElement(setting: Setting<Color>, x: Float, y: Float, width: Float, height: Float) : SettingElement<Color>(setting, x, y, width, height) {

    private val hueSetting = Setting("Hue", 0f, 0f, 360f, 1f) as Setting<Number>
    private val saturationSetting = Setting("Saturation", 0f, 0f, 100f, 1f) as Setting<Number>
    private val brightnessSetting = Setting("Brightness", 0f, 0f, 100f, 1f) as Setting<Number>
    private val alphaSetting = Setting("Alpha", 0f, 0f, 255f, 1f) as Setting<Number>
    private val rainbowSetting = Setting("Rainbow", false)
    private val rainbowSpeedSetting = Setting("Rainbow Speed", 4f, 0.1f, 10f, 0.1f) as Setting<Number>
    private val syncSetting = Setting("Sync", false)

    init {
        val values = Color.RGBtoHSB(setting.value.red, setting.value.green, setting.value.blue, null)

        hueSetting.setValue(((values[0] * 360f).toInt()).toFloat())
        saturationSetting.setValue(((values[1] * 100f).toInt()).toFloat())
        brightnessSetting.setValue(((values[2] * 100f).toInt()).toFloat())
        alphaSetting.setValue(setting.value.alpha.toFloat())
        rainbowSetting.setValue(setting.isRainbow)
        rainbowSpeedSetting.setValue(setting.rainbowSpeed)
        syncSetting.setValue(setting.isSync)

        settings.addAll(
            listOf(
                SliderElement(hueSetting, x + 2, y, width - 4, height),
                SliderElement(saturationSetting, x + 2, y, width - 4, height),
                SliderElement(brightnessSetting, x + 2, y, width - 4, height),
                SliderElement(alphaSetting, x + 2, y, width - 4, height),
                BooleanElement(rainbowSetting, x + 2, y, width - 4, height),
                SliderElement(rainbowSpeedSetting, x + 2, y, width - 4, height),
                BooleanElement(syncSetting, x + 2, y, width - 4, height)
            )
        )
    }

    override fun draw(mouseX: Float, mouseY: Float, mouseDelta: Int) {
        val hovered = isHovered(mouseX, mouseY)

        setting.setValue(
            Color(
                Color.HSBtoRGB(
                    hueSetting.value.toFloat() / 360f,
                    saturationSetting.value.toFloat() / 100f,
                    brightnessSetting.value.toFloat() / 100f
                )
            ).integrateAlpha(alphaSetting.value.toFloat())
        )

        setting.isRainbow = rainbowSetting.value
        setting.rainbowSaturation = saturationSetting.value.toFloat()
        setting.rainbowSpeed = rainbowSpeedSetting.value.toFloat()
        setting.isSync = syncSetting.value

        RenderUtil.drawRect(
            x,
            y,
            width,
            height,
            setting.value.integrateAlpha(if (hovered) 205f else setting.value.alpha.toFloat())
        )

        glScalef(0.85f, 0.85f, 0.85f).let {
            val factor = 1 / 0.85f

            FontUtil.drawStringWithShadow(setting.name, (x + 4) * factor, (y + 4) * factor, Color.WHITE)

            glScalef(factor, factor, factor)
        }

        super.draw(mouseX, mouseY, mouseDelta)
    }

}