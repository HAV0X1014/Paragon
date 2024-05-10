package com.paragon.impl.module.hud.impl
import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.client.ClientFont
import com.paragon.impl.setting.Setting
import com.paragon.util.mc
import com.paragon.util.render.ColourUtil
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.ColourUtil.toColour
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Easing
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.math.MathHelper
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color

@SideOnly(Side.CLIENT)
object ArrayListHUD : Module("ArrayList", Category.HUD, "Renders the enabled modules on screen") {

    private val anchor = Setting("Anchor", Anchor.TOP_RIGHT) describedBy "Where the ArrayList is positioned"
    private val margin = Setting("Margin", 5f, 0f, 20f, 1f) describedBy "The margin between the screen bounds and the array list"
    private val colour = Setting("Colour", Colour.GRADIENT) describedBy "How to colour the elements"
    private val colourValue = Setting("ColourValue", Color.WHITE) describedBy "The text colour" visibleWhen { colour.value == Colour.VALUE }
    private val startHue = Setting("StartHue", 0f, 0f, 360f, 1f) describedBy "The start hue of the gradient" visibleWhen { colour.value == Colour.GRADIENT }
    private val endHue = Setting("EndHue", 360f, 0f, 360f, 1f) describedBy "The end hue of the gradient" visibleWhen { colour.value == Colour.GRADIENT }
    private val saturation = Setting("Saturation", 100f, 0f, 100f, 1f) describedBy "The saturation of the colour" visibleWhen { colour.value != Colour.VALUE }
    private val brightness = Setting("Brightness", 100f, 0f, 100f, 1f) describedBy "The brightness of the colour" visibleWhen { colour.value == Colour.GRADIENT }
    private val speed = Setting("Speed", 4f, 0.1f, 10f, 0.1f) describedBy "The speed of the rainbow" visibleWhen { colour.value == Colour.WAVE }
    private val background = Setting("Background", Color(0, 0, 0, 150)) describedBy "The colour of the background"
    private val backgroundSync = Setting("BackgroundSync", false) describedBy "Sync the background colour to the text colour"
    private val moduleHeight = Setting("ModuleHeight", 13f, 12f, 20f, 1f) describedBy "The height of each module"
    private val dataMode = Setting("Data", Data.PLAIN) describedBy "How to render the module data"
    private val scissorSlide = Setting("ScissorSlide", false) describedBy "Whether the scissor will horizontally reveal the module"
    private val sideBar = Setting("SideBar", true) describedBy "Draw a bar at the side of the ArrayList"
    private val moduleBar = Setting("ModuleBar", false) describedBy "Draw a bar at the side of each module"
    private val topBar = Setting("TopBar", true) describedBy "Draw a bar at the top of the ArrayList"
    private val connect = Setting("Connect", false) describedBy "Connect each module with a bar"

    val animationSpeed = Setting("AnimationSpeed", 200f, 0f, 1500f, 5f) describedBy "The speed of the module animations"
    val easing = Setting("Easing", Easing.LINEAR) describedBy "The easing type of the animation" excludes Easing.BACK_IN

    override fun onRender2D() {
        val scaledResolution = ScaledResolution(mc)

        val modules = Paragon.INSTANCE.moduleManager.getModulesThroughPredicate { it.animation.getAnimationFactor() > 0 && it.isVisible() }
                .sortedBy { FontUtil.getStringWidth(it.name + if (dataMode.value != Data.OFF) "" + if (it.getData().isNotEmpty()) " " + dataMode.value.first + it.getData() + dataMode.value.second else "" else "") }
                .reversed()

        when (anchor.value) {
            Anchor.TOP_LEFT -> {
                val x = margin.value
                var y = margin.value + if (topBar.value) 1f else 0f

                modules.forEachIndexed { index, module ->
                    val data = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                    val info = module.name + TextFormatting.GRAY + data

                    val factor = module.animation.getAnimationFactor().coerceAtLeast(0.0)

                    if (factor != 1.0) {
                        RenderUtil.pushScissor(
                            (x * if (scissorSlide.value) factor.toFloat() else 1f) - 3,
                            y - 1,
                            FontUtil.getStringWidth(info) + 12f,
                            ((moduleHeight.value + 1) * factor).toFloat() + 3f
                        )
                    }

                    val colour: Color = when (colour.value) {
                        Colour.VALUE -> colourValue.value

                        Colour.GRADIENT -> {
                            val indexHue = index.toFloat() / modules.size.toFloat()
                            Color.HSBtoRGB((startHue.value + ((endHue.value - startHue.value) * indexHue).coerceAtLeast(startHue.value)).coerceIn(0f, 360f) / 360f, saturation.value / 100, brightness.value / 100).toColour()
                        }

                        Colour.WAVE -> ColourUtil.getRainbow(speed.value, saturation.value / 100, index * 50).toColour()
                    }

                    RenderUtil.drawRect(
                        x - 2,
                        y,
                        FontUtil.getStringWidth(info) + 8 + if (sideBar.value) 1f else 0f,
                        (moduleHeight.value * module.animation.getAnimationFactor()).toFloat(),

                        if (backgroundSync.value) {
                            colour.integrateAlpha(background.value.alpha.toFloat())
                        } else {
                            background.value
                        }
                    )

                    FontUtil.drawStringWithShadow(
                        info,
                        x + 1,
                        y + ((moduleHeight.value / 2) - (FontUtil.getHeight() / 2)),
                        colour.integrateAlpha(MathHelper.clamp((255 * module.animation.getAnimationFactor()).toFloat(), 5f, 255f))
                    )

                    if (index == 0 && topBar.value) {
                        RenderUtil.drawRect(
                            x - 3,
                            y - 1,
                            FontUtil.getStringWidth(info) + (if (moduleBar.value) 8 else 7) + if (sideBar.value) 2 else 0,
                            1f,
                            colour
                        )
                    }

                    if (moduleBar.value) {
                        RenderUtil.drawRect(
                            x + FontUtil.getStringWidth(info) + 6,
                            y,
                            1f,
                            moduleHeight.value,
                            colour
                        )
                    }

                    if (connect.value)  {
                        val width = if (index == modules.size - 1) {
                            FontUtil.getStringWidth(info) + 8
                        } else {
                            val module = modules[index + 1]
                            val nextData = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                            val nextInfo = module.name + TextFormatting.GRAY + nextData

                            (FontUtil.getStringWidth(info) + 4) - (FontUtil.getStringWidth(nextInfo) + 4)
                        }

                        RenderUtil.drawRect(x + FontUtil.getStringWidth(info) + 6 - width + if (sideBar.value) 0f else 1f, y + moduleHeight.value - 1, width, 1f, colour)
                    }

                    if (sideBar.value) {
                        RenderUtil.drawRect(x - 3f, y, 1f, moduleHeight.value, colour)
                    }

                    if (factor != 1.0) {
                        RenderUtil.popScissor()
                    }

                    y += (moduleHeight.value * module.animation.getAnimationFactor()).toFloat()
                }
            }

            Anchor.TOP_RIGHT -> {
                val x = (scaledResolution.scaledWidth - margin.value) - 2 - if (sideBar.value) 2f else 0f
                var y = margin.value + if (topBar.value) 1f else 0f

                modules.forEachIndexed { index, module ->
                    val data = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                    val info = module.name + TextFormatting.GRAY + data

                    val factor = module.animation.getAnimationFactor().coerceAtLeast(0.0)

                    if (factor != 1.0) {
                        RenderUtil.pushScissor(
                            (x - FontUtil.getStringWidth(info) * if (scissorSlide.value) factor.toFloat() else 1f) - 3,
                            y - 1,
                            FontUtil.getStringWidth(info) + 7f,
                            ((moduleHeight.value + 1) * factor).toFloat() + 3f
                        )
                    }

                    val colour: Color = when (colour.value) {
                        Colour.VALUE -> colourValue.value

                        Colour.GRADIENT -> {
                            val indexHue = index / modules.size.toFloat()
                            Color.HSBtoRGB(
                                (startHue.value + ((endHue.value - startHue.value) * indexHue).coerceAtLeast(startHue.value)).coerceIn(0f, 360f) / 360f,
                                saturation.value / 100,
                                brightness.value / 100
                            ).toColour()
                        }

                        Colour.WAVE -> ColourUtil.getRainbow(speed.value, saturation.value / 100, index * 50).toColour()
                    }

                    RenderUtil.drawRect(
                        x - FontUtil.getStringWidth(info) - 2,
                        y,
                        FontUtil.getStringWidth(info) + 4 + if (sideBar.value) 1f else 0f,
                        (moduleHeight.value * module.animation.getAnimationFactor()).toFloat(),

                        if (backgroundSync.value) {
                            colour.integrateAlpha(background.value.alpha.toFloat())
                        } else {
                            background.value
                        }
                    )

                    FontUtil.drawStringWithShadow(
                        info,
                        x - FontUtil.getStringWidth(info),
                        y + ((moduleHeight.value / 2) - (FontUtil.getHeight() / 2)),
                        colour.integrateAlpha(MathHelper.clamp((255 * module.animation.getAnimationFactor()).toFloat(), 5f, 255f))
                    )

                    if (index == 0 && topBar.value) {
                        RenderUtil.drawRect(
                            x - FontUtil.getStringWidth(info) - 2 - (if (moduleBar.value) 1 else 0),
                            y - 1,
                            FontUtil.getStringWidth(info) + (if (moduleBar.value) 5 else 4) + if (sideBar.value) 2 else 0,
                            1f,
                            colour
                        )
                    }

                    if (moduleBar.value) {
                        RenderUtil.drawRect(
                            x - FontUtil.getStringWidth(info) - 3,
                            y,
                            1f,
                            moduleHeight.value * module.animation.getAnimationFactor().toFloat(),
                            colour
                        )
                    }

                    if (connect.value)  {
                        val width = if (index == modules.size - 1) {
                            FontUtil.getStringWidth(info) + 5
                        } else {
                            val module = modules[index + 1]
                            val nextData = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                            val nextInfo = module.name + TextFormatting.GRAY + nextData

                            (FontUtil.getStringWidth(info) + 4) - (FontUtil.getStringWidth(nextInfo) + 4)
                        }

                        RenderUtil.drawRect(x - FontUtil.getStringWidth(info) - 2, y + moduleHeight.value - 1, width, 1f, colour)
                    }

                    if (sideBar.value) {
                        RenderUtil.drawRect(x + 3f, y, 1f, moduleHeight.value * module.animation.getAnimationFactor().toFloat(), colour)
                    }

                    if (factor != 1.0) {
                        RenderUtil.popScissor()
                    }

                    y += (moduleHeight.value * module.animation.getAnimationFactor()).toFloat()
                }
            }

            Anchor.BOTTOM_RIGHT -> {
                val x = (scaledResolution.scaledWidth - margin.value) - 2 - if (sideBar.value) 2f else 0f
                var y = scaledResolution.scaledHeight - margin.value - moduleHeight.value - if (topBar.value) 1f else 0f

                modules.forEachIndexed { index, module ->
                    val data = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                    val info = module.name + TextFormatting.GRAY + data

                    val factor = module.animation.getAnimationFactor().coerceAtLeast(0.0)

                    if (factor != 1.0) {
                        RenderUtil.pushScissor(
                            (x - FontUtil.getStringWidth(info) * if (scissorSlide.value) factor.toFloat() else 1f) - 3f,
                            y - 1,
                            FontUtil.getStringWidth(info) + 7f,
                            ((moduleHeight.value + 1) * factor).toFloat() + 3
                        )
                    }

                    val colour: Color = when (colour.value) {
                        Colour.VALUE -> colourValue.value

                        Colour.GRADIENT -> {
                            val indexHue = index.toFloat() / modules.size.toFloat()
                            Color.HSBtoRGB(
                                (startHue.value + ((endHue.value - startHue.value) * indexHue).coerceAtLeast(startHue.value)).coerceIn(0f, 360f) / 360f,
                                saturation.value / 100,
                                brightness.value / 100
                            ).toColour()
                        }

                        Colour.WAVE -> ColourUtil.getRainbow(speed.value, saturation.value / 100, index * 50).toColour()
                    }

                    RenderUtil.drawRect(
                        x - FontUtil.getStringWidth(info) - 2,
                        y,
                        FontUtil.getStringWidth(info) + 4 + if (sideBar.value) 1f else 0f,
                        (moduleHeight.value * module.animation.getAnimationFactor()).toFloat(),

                        if (backgroundSync.value) {
                            colour.integrateAlpha(background.value.alpha.toFloat())
                        } else {
                            background.value
                        }
                    )

                    FontUtil.drawStringWithShadow(
                        info,
                        x - FontUtil.getStringWidth(info),
                        y + ((moduleHeight.value / 2) - (FontUtil.getHeight() / 2)),
                        colour.integrateAlpha(MathHelper.clamp((255 * module.animation.getAnimationFactor()).toFloat(), 5f, 255f))
                    )

                    if (index == 0 && topBar.value) {
                        RenderUtil.drawRect(
                            x - FontUtil.getStringWidth(info) - 2 - (if (moduleBar.value) 1 else 0),
                            y + moduleHeight.value,
                            FontUtil.getStringWidth(info) + (if (moduleBar.value) 5 else 4) + if (sideBar.value) 2 else 0,
                            1f,
                            colour
                        )
                    }

                    if (moduleBar.value) {
                        RenderUtil.drawRect(
                            x - FontUtil.getStringWidth(info) - 3,
                            y,
                            1f,
                            moduleHeight.value,
                            colour
                        )
                    }

                    if (connect.value)  {
                        val width = if (index == modules.size - 1) {
                            FontUtil.getStringWidth(info) + 5
                        } else {
                            val module = modules[index + 1]
                            val nextData = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                            val nextInfo = module.name + TextFormatting.GRAY + nextData

                            (FontUtil.getStringWidth(info) + 4) - (FontUtil.getStringWidth(nextInfo) + 4)
                        }

                        RenderUtil.drawRect(x - FontUtil.getStringWidth(info) - 2, y, width, 1f, colour)
                    }

                    if (sideBar.value) {
                        RenderUtil.drawRect(x + 3f, y, 1f, moduleHeight.value, colour)
                    }

                    if (factor != 1.0) {
                        RenderUtil.popScissor()
                    }

                    y -= (moduleHeight.value * module.animation.getAnimationFactor()).toFloat()
                }
            }

            Anchor.BOTTOM_LEFT -> {
                val x = margin.value - if (sideBar.value) 1f else 2f
                var y = scaledResolution.scaledHeight - margin.value - moduleHeight.value - if (topBar.value) 1f else 0f

                modules.forEachIndexed { index, module ->
                    val data = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                    val info = module.name + TextFormatting.GRAY + data

                    val factor = module.animation.getAnimationFactor().coerceAtLeast(0.0)

                    if (factor != 1.0) {
                        RenderUtil.pushScissor(
                            x + 3f,
                            y - 1f,
                            FontUtil.getStringWidth(info) + 7f,
                            ((moduleHeight.value + 1) * factor).toFloat() + 3f
                        )
                    }

                    val colour: Color = when (colour.value) {
                        Colour.VALUE -> colourValue.value

                        Colour.GRADIENT -> {
                            val indexHue = index.toFloat() / modules.size.toFloat()
                            Color.HSBtoRGB(
                                (startHue.value + ((endHue.value - startHue.value) * indexHue).coerceAtLeast(startHue.value)).coerceIn(0f, 360f) / 360f,
                                saturation.value / 100,
                                brightness.value / 100
                            ).toColour()
                        }

                        Colour.WAVE -> ColourUtil.getRainbow(speed.value, saturation.value / 100, index * 50).toColour()
                    }

                    RenderUtil.drawRect(
                        x + 2,
                        y,
                        FontUtil.getStringWidth(info) + 4 + if (sideBar.value) 1f else 0f,
                        (moduleHeight.value * module.animation.getAnimationFactor()).toFloat(),

                        if (backgroundSync.value) {
                            colour.integrateAlpha(background.value.alpha.toFloat())
                        } else {
                            background.value
                        }
                    )

                    FontUtil.drawStringWithShadow(
                        info,
                        x + 4,
                        y + ((moduleHeight.value / 2) - (FontUtil.getHeight() / 2)),
                        colour.integrateAlpha(MathHelper.clamp((255 * module.animation.getAnimationFactor()).toFloat(), 5f, 255f))
                    )

                    if (index == 0 && topBar.value) {
                        RenderUtil.drawRect(
                            x + (if (moduleBar.value) 2 else 1) + if (sideBar.value) -1 else 0,
                            y + moduleHeight.value,
                            FontUtil.getStringWidth(info) + (if (moduleBar.value) 5 else 4) + if (sideBar.value) 1 else 1,
                            1f,
                            colour
                        )
                    }

                    if (moduleBar.value) {
                        RenderUtil.drawRect(
                            x + 6 + FontUtil.getStringWidth(info),
                            y,
                            1f,
                            moduleHeight.value,
                            colour
                        )
                    }

                    if (connect.value)  {
                        val width = if (index == modules.size - 1) {
                            FontUtil.getStringWidth(info) + 5
                        } else {
                            val module = modules[index + 1]
                            val nextData = if (dataMode.value != Data.OFF) if (module.getData().isNotEmpty()) " " + dataMode.value.first + module.getData() + dataMode.value.second else "" else ""
                            val nextInfo = module.name + TextFormatting.GRAY + nextData

                            (FontUtil.getStringWidth(info) + 4) - (FontUtil.getStringWidth(nextInfo) + 4)
                        }

                        RenderUtil.drawRect(x + FontUtil.getStringWidth(info) + 6 - width + if (sideBar.value) 0f else 1f, y, width, 1f, colour)
                    }

                    if (sideBar.value) {
                        RenderUtil.drawRect(x + 1, y, 1f, moduleHeight.value, colour)
                    }

                    if (factor != 1.0) {
                        RenderUtil.popScissor()
                    }

                    y -= (moduleHeight.value * module.animation.getAnimationFactor()).toFloat()
                }
            }
        }
    }

    enum class Anchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    enum class Data(val first: String, val second: String) {
        PLAIN("", ""),
        SQUARE("[", "]"),
        CURLY("{", "}"),
        OFF("", "")
    }

    enum class Colour {
        VALUE,
        GRADIENT,
        WAVE
    }

}