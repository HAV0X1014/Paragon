package com.paragon.impl.ui.configuration.discord.module

import com.paragon.impl.module.Module
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.discord.IRenderable
import com.paragon.impl.ui.configuration.discord.category.CategoryBar
import com.paragon.impl.ui.configuration.discord.settings.SettingsBar
import com.paragon.util.mc
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.RenderUtil.scaleTo
import com.paragon.util.render.font.FontUtil
import com.paragon.util.render.font.FontUtil.drawCenteredString
import com.paragon.util.render.font.FontUtil.drawStringWithShadow
import com.paragon.util.render.font.FontUtil.getStringWidth
import me.surge.animation.Animation
import me.surge.animation.Easing
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.util.Rectangle
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlin.math.min

/**
 * @author SooStrator1136
 */
object ModuleBar : IRenderable {

    var focusedModule: Module? = null

    var scrollOffset = 0

    val shownModules: MutableList<DiscordModule> = ArrayList()
    val rect = Rectangle()
    private val userRect = Rectangle()

    private val nameAnimation = Animation({ 2000F }, false, { Easing.LINEAR })
    var lastCopyTime = 0L

    override fun render(mouseX: Int, mouseY: Int) {
        //Set the basic bounds
        run {
            rect.setBounds(
                CategoryBar.rect.x + CategoryBar.rect.width, CategoryBar.rect.y, CategoryBar.rect.width * 3, CategoryBar.rect.height - 30
            )
            userRect.setBounds(
                rect.x, rect.y + rect.height, rect.width, 30
            )
        }

        //Render the actual modules
        run {
            //Scroll logic
            if (DiscordGUI.dWheel != 0 && shownModules.isNotEmpty() && rect.contains(mouseX, mouseY)) {
                val lastRect = shownModules[shownModules.size - 1].rect

                val maxOffset = min(((((lastRect.y + lastRect.height) - shownModules[0].rect.y) - rect.height) * -1), 0)
                val newScrollOffset = scrollOffset + (DiscordGUI.dWheel / 13)
                if (DiscordGUI.dWheel < 0) {
                    scrollOffset = if (newScrollOffset < maxOffset) maxOffset else newScrollOffset
                }
                else if (scrollOffset < 0) {
                    scrollOffset = if (newScrollOffset > 0) 0 else newScrollOffset
                }
            }

            var yOffset = 0
            shownModules.forEachIndexed { i, module ->
                module.rect.setBounds(rect.x + 2, rect.y + 2 + yOffset + scrollOffset, rect.width - 4, 20)
                yOffset = (20 * (i + 1)) + (5 * (i + 1))
            }

            RenderUtil.drawRect(
                rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat(), DiscordGUI.channelBarBackground
            )

            RenderUtil.pushScissor(
                rect.x.toFloat(),
                rect.y + 1f,
                rect.width.toFloat(),
                rect.height.toFloat(),
            )

            shownModules.forEach {
                it.render(mouseX, mouseY)
            }

            RenderUtil.popScissor()
        }

        //Render the user info
        run {
            RenderUtil.drawRect(
                userRect.x.toFloat(), userRect.y.toFloat(), userRect.width.toFloat(), userRect.height.toFloat(), DiscordGUI.userFieldBackground
            )
            renderHead(userRect.x + 3, userRect.y + 2, 20)

            if (getStringWidth(mc.player.name) > userRect.width - 30.0) {
                nameAnimation.state = userRect.contains(mouseX, mouseY)
            }

            RenderUtil.pushScissor(
                userRect.x + 30f, userRect.y.toFloat(), userRect.width - 30f, userRect.height.toFloat()
            )
            drawStringWithShadow(
                mc.player.name, (((userRect.x + 30F) - ((getStringWidth(mc.player.name) - (userRect.width - 30F)) * nameAnimation.getAnimationFactor())).toFloat()), (userRect.y + (userRect.height / 2F)) - (FontUtil.getHeight() / 2), Color.WHITE
            )
            RenderUtil.popScissor()

            // Render the "copied" thing after the name was copied
            if (lastCopyTime != 0L) {
                RenderUtil.drawRoundedRect(
                    (userRect.x + ((userRect.width - getStringWidth("Copied!")) / 2f)) - 5f, (userRect.y - (FontUtil.getHeight() / 2f)) - 1.5f, getStringWidth("Copied!") + 10f, FontUtil.getHeight() + 5f, 2f, DiscordGUI.userCopiedColor
                )

                drawCenteredString("Copied!", (userRect.x + (userRect.width / 2)).toFloat(), userRect.y.toFloat() - 3f, Color.WHITE)

                if (System.currentTimeMillis() - 1500L > lastCopyTime) {
                    lastCopyTime = 0L
                }
            }
        }
    }

    private fun renderHead(x: Int, y: Int, size: Int) {
        mc.textureManager.bindTexture(mc.player.locationSkin)
        val scaleFac = size / 32.0
        scaleTo(x.toFloat(), y.toFloat(), 0F, scaleFac, scaleFac, 1.0) {
            GlStateManager.color(1F, 1F, 1F, 1F)
            (mc.currentScreen ?: return@scaleTo).drawTexturedModalRect(x + 5, y + 5, 32, 32, 32, 32)
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (userRect.contains(mouseX, mouseY)) {
            lastCopyTime = System.currentTimeMillis()
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(mc.player.name), null)
        }
        else if (rect.contains(mouseX, mouseY)) {
            for (module in shownModules) {
                if (module.rect.contains(mouseX, mouseY)) {
                    focusedModule = module.module
                    SettingsBar.shownSettings.clear()
                    SettingsBar.scrollOffset = 0
                    break
                }
            }
        }
    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {}

    override fun onKey(keyCode: Int) {}

}