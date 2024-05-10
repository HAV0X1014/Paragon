package com.paragon.impl.ui.menu

import com.paragon.Paragon
import com.paragon.util.render.BlurUtil
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.RenderUtil
import me.surge.animation.Animation
import me.surge.animation.ColourAnimation
import me.surge.animation.Easing
import net.minecraft.client.gui.*
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.awt.Desktop
import java.net.URI

/**
 * @author Surge
 */
class ParagonMenu : GuiScreen() {

    private val buttons = arrayOf(
        Button(FontUtil.Icon.PERSON, "Singleplayer", (width / 2f) - 66, height - 70f) {
            mc.displayGuiScreen(GuiWorldSelection(this))
        },
        Button(FontUtil.Icon.PEOPLE, "Multiplayer", (width / 2f) + 2, height - 70f) {
            mc.displayGuiScreen(GuiMultiplayer(this))
        },
        Button(FontUtil.Icon.GEAR, "Options", (width / 2f) - 66, height - 70f) {
            mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
        },
        Button(FontUtil.Icon.GITHUB, "GitHub", (width / 2f) + 2, height - 70f) {
            Desktop.getDesktop().browse(URI("https://github.com/Wolfsurge/Paragon"))
        },
        Button(FontUtil.Icon.CHAT, "Discord", (width / 2f) + 2, height - 70f) {
            Desktop.getDesktop().browse(URI("https://discord.gg/28JNQsXUzb"))
        },
        Button(FontUtil.Icon.CLOSE, "Exit", (width / 2f) + 2, height - 70f) {
            mc.shutdown()
        }
    )

    private val whiteFade = ColourAnimation(Color.WHITE, Color(0, 0, 0, 0), { 1200f }, false, { Easing.LINEAR })
    private val buttonSlide = Animation({ 400f }, false, { Easing.CIRC_IN_OUT })

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val xOffset = -1.0f * ((mouseX - width / 10.0f) / (width / 10.0f))
        val yOffset = -1.0f * ((mouseY - height / 10.0f) / (height / 10.0f))

        mc.textureManager.bindTexture(ResourceLocation("paragon", "textures/background.png"))
        RenderUtil.drawModalRectWithCustomSizedTexture(xOffset, yOffset, 0f, 0f, width.toFloat() + 20, height.toFloat() + 20, width.toFloat() + 20, height.toFloat() + 20)
        BlurUtil.blur(0f, 0f, width.toFloat(), height.toFloat(), 20f)

        RenderUtil.scaleTo((width / 2f) - FontUtil.fontLarge.getStringWidth("Paragon"), 35f, 0f, 2.0, 2.0, 0.0) {
            FontUtil.fontLarge.drawString(
                "Paragon",
                (width / 2f) - FontUtil.fontLarge.getStringWidth("Paragon"),
                35f,
                Color.WHITE,
                false
            )
        }

        FontUtil.drawString(
            "Version ${ Paragon.VERSION }",
            3f,
            3f,
            Color(180, 180, 180)
        )

        var x = (width / 2f) - ((buttons.size * 68f) / 2f)
        var yIncrease = 0

        buttons.forEach {
            it.x = x
            it.y = (height - (120 + (yIncrease - (yIncrease * buttonSlide.getAnimationFactor()) * buttonSlide.getAnimationFactor())) * buttonSlide.getAnimationFactor()).toFloat()

            it.render(mouseX, mouseY)

            x += 68
            yIncrease += 20
        }

        super.drawScreen(mouseX, mouseY, partialTicks)

        whiteFade.state = true
        buttonSlide.state = whiteFade.getAnimationFactor() == 1.0
        RenderUtil.drawRect(0f, 0f, width.toFloat(), height.toFloat(), whiteFade.getColour())
    }

    override fun onGuiClosed() {
        whiteFade.resetToDefault()
        buttonSlide.resetToDefault()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        buttons.forEach { it.mouseClicked() }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    internal class Button(val icon: FontUtil.Icon, val name: String, var x: Float, var y: Float, val onClick: Runnable) {
        val hover = Animation({ 200f }, false, { Easing.CUBIC_IN_OUT })

        fun render(mouseX: Int, mouseY: Int) {
            hover.state = mouseX.toFloat() in x..x + 64 && mouseY.toFloat() in y..y + 64

            RenderUtil.scaleTo(x + 32, y + 32, 0f, 1 - (0.25 * hover.getAnimationFactor()), 1 - (0.25 * hover.getAnimationFactor()), 1.0) {
                RenderUtil.drawRoundedRect(
                    x,
                    y - (16 * hover.getAnimationFactor()).toFloat(),
                    64f,
                    64f,
                    31f, // 32, despite being exactly half, just feels kinda off? not sure why, but 31 looks good enough
                    Color(0, 0, 0, 150)
                )

                RenderUtil.scaleTo(
                    x + 11f + if (icon == FontUtil.Icon.PERSON || icon == FontUtil.Icon.CLOSE) 7f else 0f + if (icon == FontUtil.Icon.GEAR) 4f else 0f + if (icon == FontUtil.Icon.GITHUB) 1f else 0f,
                    y + 10 - (16 * hover.getAnimationFactor()).toFloat() + if (icon == FontUtil.Icon.GEAR || icon == FontUtil.Icon.CLOSE) 2f else 0f + if (icon == FontUtil.Icon.GITHUB) 1f else 0f,
                    0f,
                    2.0,
                    2.0,
                    0.0
                ) {
                    FontUtil.drawIcon(
                        icon,
                        x + 11f + if (icon == FontUtil.Icon.PERSON || icon == FontUtil.Icon.CLOSE) 7f else 0f + if (icon == FontUtil.Icon.GEAR) 4f else 0f + if (icon == FontUtil.Icon.GITHUB) 1f else 0f,
                        y + 10 - (16 * hover.getAnimationFactor()).toFloat() + if (icon == FontUtil.Icon.GEAR || icon == FontUtil.Icon.CLOSE) 2f else 0f + if (icon == FontUtil.Icon.GITHUB) 1f else 0f,
                        Color.WHITE
                    )
                }
            }

            FontUtil.font.drawString(name, (x + 32) - (FontUtil.font.getStringWidth(name) / 2), y + 64 - (16 * hover.getAnimationFactor()).toFloat(), Color(255, 255, 255, (255 * hover.getAnimationFactor()).toInt().coerceAtLeast(5)), false)
        }

        fun mouseClicked() {
            if (hover.state) {
                onClick.run()
            }
        }

    }

}