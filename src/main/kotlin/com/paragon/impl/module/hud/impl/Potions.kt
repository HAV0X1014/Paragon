package com.paragon.impl.module.hud.impl

import com.google.common.collect.Ordering
import com.paragon.impl.module.client.Colours
import com.paragon.impl.module.hud.HUDModule
import com.paragon.impl.setting.Setting
import com.paragon.util.mc
import com.paragon.util.render.ColourUtil
import com.paragon.util.render.ColourUtil.glColour
import com.paragon.util.render.ColourUtil.integrateAlpha
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.scaleTo
import com.paragon.util.render.font.FontUtil
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import java.awt.Color

/**
 * @author Surge
 * @since 25/12/2022
 */
object Potions : HUDModule("Potions", "Draws your active potion effects on screen", { 140f }, { 10f }) {

    private val mode = Setting("Mode", Mode.PYRO) describedBy "How effects are drawn"
    private val rainbowSpeed = Setting("Rainbow speed", 20f, 5f, 50f, 2.5f) visibleWhen { mode.value == Mode.PYRO }
    private val showBg = Setting("Background", true) visibleWhen { mode.value == Mode.PYRO }
    private val syncTextColor = Setting("Sync text", false) visibleWhen { mode.value == Mode.PYRO }
    private val offset = Setting("Offset", 0f, 0f, 10f, 1f) describedBy "The offset between the effects"

    override fun draw() {
        val activeEffects = mc.player.activePotionEffects

        if (activeEffects.isEmpty()) {
            return
        }

        when (mode.value) {
            Mode.INVENTORY -> {
                Color.WHITE.glColour()
                GlStateManager.disableLighting()

                var effectY = y

                var entryHeight = ((FontUtil.getHeight() * 2.5f) + 3) + offset.value

                if (activeEffects.size > 5) {
                    entryHeight = (132 / (activeEffects.size - 1)) + offset.value
                }

                Ordering.natural<Comparable<*>>().sortedCopy(activeEffects).forEach { effect ->
                    val potion = effect.potion

                    if (!potion.shouldRender(effect)) {
                        return@forEach
                    }

                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                    mc.textureManager.bindTexture(GuiContainer.INVENTORY_BACKGROUND)

                    if (potion.hasStatusIcon()) {
                        val iconIndex = potion.statusIconIndex

                        Color.WHITE.glColour()
                        drawTexturedModalRect(x.toInt(), effectY.toInt() + 7, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18)
                    }

                    if (!potion.shouldRenderInvText(effect)) {
                        effectY += entryHeight
                        return@forEach
                    }

                    val effectName = "${I18n.format(potion.name)} ${I18n.format("enchantment.level.${effect.amplifier + 1}")}"
                    FontUtil.drawStringWithShadow(effectName, x + 22, effectY + 6, Color.WHITE)

                    FontUtil.drawStringWithShadow(Potion.getPotionDurationString(effect, 1f), x + 22, effectY + 16, Color(127, 127, 127))

                    effectY += entryHeight
                }
            }

            Mode.PYRO -> {
                var effectY = y

                val maxWidth = activeEffects.maxWith(Comparator.comparingDouble {
                    FontUtil.getStringWidth(I18n.format(it.potion.name) + " " + I18n.format("enchantment.level.${it.amplifier + 1}") + " ${Potion.getPotionDurationString(it, 1f)}").toDouble()
                }).let {
                    FontUtil.getStringWidth(I18n.format(it.potion.name) + " " + I18n.format("enchantment.level.${it.amplifier + 1}") + " ${Potion.getPotionDurationString(it, 1f)}") + FontUtil.getHeight() + 1f
                } + 2 // 💀

                activeEffects.forEach { effect ->
                    val color = Color(ColourUtil.getRainbow(rainbowSpeed.value, Colours.mainColour.rainbowSaturation / 100f, (effectY * effectY).toInt()))

                    if (showBg.value) {
                        RenderUtil.drawRect(x, effectY, maxWidth, FontUtil.getHeight() + 3f, color.integrateAlpha(100f))
                        RenderUtil.drawBorder(x, effectY, maxWidth, FontUtil.getHeight() + 3f, 0.5f, color.darker())
                    }

                    val scaleFac = FontUtil.getHeight() / 18.0

                    scaleTo(x, effectY, 0f, scaleFac, scaleFac, 1.0) {
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                        GlStateManager.disableLighting()
                        mc.textureManager.bindTexture(GuiContainer.INVENTORY_BACKGROUND)

                        if (effect.potion.hasStatusIcon()) {
                            val iconIndex = effect.potion.statusIconIndex

                            Color.WHITE.glColour()
                            drawTexturedModalRect(x.toInt(), effectY.toInt() + 2, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18)
                        }
                    }

                    FontUtil.drawStringWithShadow(I18n.format(effect.potion.name) + " " + I18n.format("enchantment.level.${effect.amplifier + 1}") + " ${Potion.getPotionDurationString(effect, 1f)}", x + FontUtil.getHeight() + 1f, effectY + 2f, if (syncTextColor.value) color else Color.WHITE)

                    effectY += FontUtil.getHeight() + offset.value + 3.5f
                }
            }
        }
    }

    override fun dummy() {
        FontUtil.drawString("Potions", x + 1, y + 1, Color.WHITE)
    }

    private fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int) {
        val tessellator = Tessellator.getInstance()

        val bufferbuilder = tessellator.buffer
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX)

        bufferbuilder.pos((x + 0).toDouble(), (y + height).toDouble(), 0.0).tex(((textureX + 0).toFloat() * 0.00390625f).toDouble(), ((textureY + height).toFloat() * 0.00390625f).toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + height).toDouble(), 0.0).tex(((textureX + width).toFloat() * 0.00390625f).toDouble(), ((textureY + height).toFloat() * 0.00390625f).toDouble()).endVertex()
        bufferbuilder.pos((x + width).toDouble(), (y + 0).toDouble(), 0.0).tex(((textureX + width).toFloat() * 0.00390625f).toDouble(), ((textureY + 0).toFloat() * 0.00390625f).toDouble()).endVertex()
        bufferbuilder.pos((x + 0).toDouble(), (y + 0).toDouble(), 0.0).tex(((textureX + 0).toFloat() * 0.00390625f).toDouble(), ((textureY + 0).toFloat() * 0.00390625f).toDouble()).endVertex()

        tessellator.draw()
    }

    internal enum class Mode {
        /**
         * Renders effects similar to how they are shown in the inventory
         */
        INVENTORY,

        /**
         * Similar to the Pyro client's style
         */
        PYRO
    }

}