package com.paragon.impl.ui.configuration.discord.category

import com.paragon.impl.module.Category
import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.discord.IRenderable
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.scaleTo
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.util.render.font.FontUtil.getStringWidth
import com.paragon.util.string.StringUtil
import me.surge.animation.Animation
import net.minecraft.item.ItemStack
import java.awt.Color
import java.awt.Rectangle

/**
 * @author SooStrator1136
 */
class DiscordCategory(val category: Category) : IRenderable {

    val rect = Rectangle()

    private val indicator = ItemStack(category.indicator)
    private val nameAnimation = Animation({ 200F }, false, ClickGUI.easing::value)

    override fun render(mouseX: Int, mouseY: Int) {
        val diff = rect.width / 10
        rect.setBounds(rect.x + diff, rect.y + diff, rect.width - (diff * 2), rect.height - (diff * 2))

        val isHovered = rect.contains(mouseX, mouseY)

        //Render the basic icon with its background
        run {
            @Suppress("IncorrectFormatting") RenderUtil.drawRoundedRect(
                rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat(), 15f, if (isHovered) DiscordGUI.channelBarBackground.brighter() else DiscordGUI.channelBarBackground
            )

            val scaleFac = rect.width / 20.0
            scaleTo(rect.x.toFloat() + 3f + if (category == Category.RENDER) 0.5f else 0f, rect.y.toFloat() + 3, 0F, scaleFac, scaleFac, 1.0) {
                RenderUtil.drawItemStack(indicator, rect.x.toFloat() + 3f + if (category == Category.RENDER || category == Category.COMBAT) 0.5f else 0f, rect.y.toFloat() + 3, false)
            }
        }

        //Render the name tooltip
        run {
            nameAnimation.state = isHovered

            RenderUtil.pushScissor(
                rect.x + rect.width - 2f, rect.y.toFloat(), (getStringWidth(StringUtil.getFormattedText(category)) + 6) * nameAnimation.getAnimationFactor().toFloat(), rect.height.toFloat()
            )

            RenderUtil.drawRoundedRect(
                (rect.x + rect.width) - 2f, (rect.centerY - (FontUtil.getHeight() / 2f)).toFloat(), getStringWidth(StringUtil.getFormattedText(category)) + 6f, FontUtil.getHeight(), 1f, DiscordGUI.categoryTextBackground
            )

            drawStringWithShadow(
                StringUtil.getFormattedText(category), (rect.x + rect.width).toFloat(), (rect.centerY - 4).toFloat(), Color.WHITE
            )

            RenderUtil.popScissor()
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {}
    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}

    override fun onKey(keyCode: Int) {}

}