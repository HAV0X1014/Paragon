@file:Suppress("ReplaceNotNullAssertionWithElvisReturn")

package com.paragon.impl.ui.configuration.discord.settings.impl

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.setting.Setting
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.util.render.font.FontUtil.getStringWidth
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.discord.settings.DiscordSetting
import com.paragon.impl.ui.util.Click
import com.paragon.util.calculations.MathsUtil.getPercent
import com.paragon.util.calculations.MathsUtil.getPercentOf
import com.paragon.util.calculations.MathsUtil.roundDouble
import com.paragon.util.calculations.MathsUtil.roundToIncrementation
import com.paragon.util.minus
import com.paragon.util.plus
import com.paragon.util.render.RenderUtil
import net.minecraft.util.math.MathHelper
import org.lwjgl.util.Rectangle
import java.awt.Color
import java.math.BigDecimal
import kotlin.math.max
import kotlin.random.Random

/**
 * @author SooStrator1136
 */
class DiscordNumber(private val setting: Setting<Number>) : DiscordSetting(setting) {

    var dragging = false

    private val sliderBounds = Rectangle()

    private val size = Random.nextInt(100, 999).toString() + "." + Random.nextInt(10, 99) + " KB"

    private val maxProgressInfoWidth = max(
        max(
            getStringWidth("${setting.min}/${setting.max}"), getStringWidth("${setting.max}/${setting.max}")
        ), max(
            getStringWidth((setting.max - setting.incrementation).toString() + "/${setting.max}"), getStringWidth((setting.min + setting.incrementation).toString() + "/${setting.max}")
        )
    )

    init {
        bounds.height = (msgStyleHeight + (FontUtil.getHeight() * 7)).toInt() + 2
    }

    override fun render(mouseX: Int, mouseY: Int) {
        super.render(mouseX, mouseY)

        val mediaRect = Rectangle(
            bounds.x, (bounds.y + msgStyleHeight).toInt(), (bounds.width * 0.75).toInt(), (FontUtil.getHeight() * 7).toInt()
        )

        val lowerMediaRect = Rectangle(
            bounds.x + 5, (mediaRect.y + (FontUtil.getHeight() * 4)).toInt(), mediaRect.width - 10, (FontUtil.getHeight() * 2).toInt()
        )

        //Render basic media background
        run {
            RenderUtil.drawRoundedRect(
                mediaRect.x.toFloat(), mediaRect.y.toFloat(), mediaRect.width.toFloat(), mediaRect.height.toFloat(), 2.5f, DiscordGUI.mediaBackground
            )
            RenderUtil.drawRoundedOutline(
                mediaRect.x.toFloat(), mediaRect.y.toFloat(), mediaRect.width.toFloat(), mediaRect.height.toFloat(), 2.5f, 2f, DiscordGUI.mediaBackgroundBorder
            )

            drawStringWithShadow(
                "${setting.name}.mp3", mediaRect.x + 5F, mediaRect.y + FontUtil.getHeight(), DiscordGUI.mediaTitle
            )
            drawStringWithShadow(
                size, mediaRect.x + 5F, mediaRect.y + (FontUtil.getHeight() * 2F), DiscordGUI.mediaSize
            )
        }

        //Render basic slider background
        run {
            RenderUtil.drawRoundedRect(
                lowerMediaRect.x.toFloat(), lowerMediaRect.y.toFloat(), lowerMediaRect.width.toFloat(), lowerMediaRect.height.toFloat(), 2.5f, DiscordGUI.mediaProgressBackground
            )

            drawStringWithShadow(
                "${setting.value}/${setting.max}", lowerMediaRect.x + 5F, lowerMediaRect.y + (FontUtil.getHeight() / 2F), Color.WHITE
            )

            sliderBounds.setBounds(
                (lowerMediaRect.x + maxProgressInfoWidth + 10).toInt(), (lowerMediaRect.y + (FontUtil.getHeight() / 2)).toInt(), ((lowerMediaRect.width - maxProgressInfoWidth) - 15).toInt() + 1, FontUtil.getHeight().toInt()
            )

            RenderUtil.drawRoundedRect(
                sliderBounds.x.toFloat(), sliderBounds.y.toFloat(), sliderBounds.width - 1f, sliderBounds.height.toFloat(), 2f, DiscordGUI.mediaProgressbarBackground
            )
        }

        //Render the progress/value
        run {
            RenderUtil.drawRoundedRect(
                sliderBounds.x.toFloat(), sliderBounds.y.toFloat(),

                // prevent funky rounded rect
                MathHelper.clamp(
                    getPercentOf(getPercent((setting.value - setting.min).toDouble(), (setting.max - setting.min).toDouble()), sliderBounds.width - 1.0), 2.0, sliderBounds.width.toDouble() - 1f
                ).toFloat(), sliderBounds.height.toFloat(), 2f, DiscordGUI.mediaProgress
            )
        }

        if (dragging) {
            setting.setValue(getNewValue(mouseX))
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(setting))
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!sliderBounds.contains(mouseX, mouseY) || button != Click.LEFT.button) {
            return
        }

        dragging = true
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {
        dragging = false
    }

    private fun getNewValue(mouseX: Int): Number {
        // hacky fix (not good)
        var toReturn: Number = roundDouble(
            roundToIncrementation(
                setting.incrementation.toDouble(), getPercentOf(
                    getPercent(
                        ((mouseX + 7) - sliderBounds.x).toDouble(), sliderBounds.width.toDouble()
                    ),
                    (setting.max - setting.min).toDouble(),
                )
            ), BigDecimal.valueOf(setting.incrementation.toDouble()).scale()
        )

        if (mouseX <= sliderBounds.x) {
            toReturn = setting.min
        }

        if (mouseX >= sliderBounds.x + sliderBounds.width) {
            toReturn = setting.max
        }

        toReturn = MathHelper.clamp(toReturn.toDouble(), setting.min.toDouble(), setting.max.toDouble())
        return if (setting.value is Float) toReturn.toFloat() else toReturn
    }

    override fun onKey(keyCode: Int) {}

}