package com.paragon.impl.ui.configuration.discord.settings

import com.paragon.impl.module.client.ClickGUI
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.discord.DiscordGUI
import com.paragon.impl.ui.configuration.discord.IRenderable
import com.paragon.impl.ui.configuration.discord.category.CategoryBar
import com.paragon.impl.ui.configuration.discord.module.ModuleBar
import com.paragon.impl.ui.configuration.discord.settings.impl.*
import com.paragon.util.render.RenderUtil
import com.paragon.util.render.font.FontUtil
import me.surge.animation.Animation
import org.lwjgl.util.Rectangle
import java.awt.Color
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author SooStrator1136
 */
object SettingsBar : IRenderable {

    var scrollOffset = 0
    private var maxScrollOffset = 0

    private const val settingOffset = 10

    private val toggleAnimation = Animation(ClickGUI.animationSpeed::value, false, ClickGUI.easing::value)
    private val toggleRect = Rectangle()
    private val toggleButton = Rectangle()

    val shownSettings: MutableList<DiscordSetting> = CopyOnWriteArrayList()
    val rect = Rectangle()

    override fun render(mouseX: Int, mouseY: Int) {
        //Set basic bounds and settings stuff
        run {
            toggleRect.setBounds(
                ModuleBar.rect.x + ModuleBar.rect.width,
                ModuleBar.rect.y,
                DiscordGUI.baseRect.width - (CategoryBar.rect.width + ModuleBar.rect.width),
                if (ModuleBar.focusedModule != null) (FontUtil.getHeight() * 4).toInt() else 0,
            )
            toggleButton.setBounds(
                (toggleRect.x + toggleRect.width) - 50,
                (toggleRect.y + FontUtil.getHeight()).toInt(),
                40,
                FontUtil.getHeight().toInt()
            )

            rect.setBounds(
                toggleRect.x,
                toggleRect.y + toggleRect.height,
                toggleRect.width,
                DiscordGUI.baseRect.height - toggleRect.height
            )

            if (rect.contains(mouseX, mouseY) && shownSettings.isNotEmpty()) {
                val lastRect = shownSettings[shownSettings.size - 1].bounds
                maxScrollOffset = Optional.of(
                    ((((lastRect.y + lastRect.height) - shownSettings[0].bounds.y) - rect.height) * -1) - 25
                ).map { if (it > 0) 0 else it }.get()
                val newOffset = scrollOffset + (DiscordGUI.dWheel / 7)
                if (DiscordGUI.dWheel < 0) {
                    scrollOffset = if (newOffset < maxScrollOffset) maxScrollOffset else newOffset
                } else if (scrollOffset < 0) {
                    scrollOffset = if (newOffset > 0) 0 else newOffset
                }
            }

            ModuleBar.focusedModule?.settings?.forEach {
                addSetting(it)
                addSubSettings(it.subsettings)
            }
        }

        RenderUtil.drawRect(
            rect.x.toFloat(), rect.y.toFloat(), rect.width.toFloat(), rect.height.toFloat(), DiscordGUI.chatBackground
        )

        //Render module toggle button
        @Suppress("ReplaceNotNullAssertionWithElvisReturn") run {
            if (ModuleBar.focusedModule != null) {
                toggleAnimation.state = !ModuleBar.focusedModule!!.isEnabled

                RenderUtil.drawRect(
                    toggleRect.x.toFloat(),
                    toggleRect.y.toFloat(),
                    toggleRect.width.toFloat(),
                    toggleRect.height.toFloat(),
                    DiscordGUI.chatBackground
                )
                RenderUtil.drawRect(
                    toggleRect.x + 10F,
                    toggleRect.y + (FontUtil.getHeight() * 3.2F),
                    toggleRect.width - 20F,
                    2F,
                    DiscordGUI.mediaSize
                )

                FontUtil.drawStringWithShadow(
                    ModuleBar.focusedModule!!.name,
                    toggleRect.x + 10F,
                    toggleRect.y + FontUtil.getHeight(),
                    DiscordGUI.channelTextColor
                )

                RenderUtil.drawRoundedRect(
                    toggleButton.x.toFloat(),
                    toggleButton.y.toFloat(),
                    toggleButton.width.toFloat(),
                    toggleButton.height.toFloat(),
                    toggleButton.height / 2f,
                    DiscordGUI.channelHoveredColor
                )

                //Indicator whether the module is toggled or not
                RenderUtil.drawRoundedRect(
                    toggleButton.x + ((toggleButton.width / 2f) * toggleAnimation.getAnimationFactor()).toFloat(),
                    toggleButton.y.toFloat(),
                    toggleButton.width / 2f,
                    toggleButton.height.toFloat(),
                    toggleButton.height / 2f,
                    if (ModuleBar.focusedModule!!.isEnabled) Color.GREEN else Color.RED
                )
            }
        }

        //Render settings
        run {
            var currY = rect.y + scrollOffset + 10
            shownSettings.forEach {
                if (shouldShow(it.dSetting)) {
                    it.bounds.setBounds(rect.x + 15, currY, rect.width - 30, it.bounds.height)
                    currY += it.bounds.height + settingOffset
                }
            }

            RenderUtil.pushScissor(
                rect.x.toFloat(), rect.y + 1f, rect.width.toFloat(), rect.height.toFloat()
            )

            shownSettings.forEach {
                if (shouldShow(it.dSetting)) {
                    it.render(mouseX, mouseY)
                }
            }

            RenderUtil.popScissor()
        }
    }

    override fun onClick(mouseX: Int, mouseY: Int, button: Int) {
        if (!rect.contains(mouseX, mouseY)) {
            if (toggleButton.contains(mouseX, mouseY)) {
                ModuleBar.focusedModule?.toggle()
            }
            return
        }
        shownSettings.forEach {
            if (shouldShow(it.dSetting)) {
                it.onClick(mouseX, mouseY, button)
            }
        }


    }

    override fun onRelease(mouseX: Int, mouseY: Int, button: Int) {
        shownSettings.forEach {
            if (shouldShow(it.dSetting)) {
                it.onRelease(mouseX, mouseY, button)
            }
        }
    }


    override fun onKey(keyCode: Int) {
        shownSettings.forEach {
            if (shouldShow(it.dSetting)) {
                it.onKey(keyCode)
            }
        }
    }

    //Adding subsettings as normal setting because Idk how subsettings in a chat type thing would work
    private fun addSubSettings(subSettings: ArrayList<Setting<*>>) {
        if (subSettings.isEmpty()) {
            return
        }
        subSettings.forEach {
            addSetting(it)
            addSubSettings(it.subsettings)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun addSetting(setting: Setting<*>) {
        if (shownSettings.any { it.dSetting == setting }) {
            return
        }
        val toAdd = when (setting.value) {
            is Boolean -> DiscordBoolean(setting as Setting<Boolean>)
            is Enum<*> -> DiscordEnum(setting as Setting<Enum<*>>)
            is String -> DiscordString(setting as Setting<String>)
            is Bind -> DiscordBind(setting as Setting<Bind>)
            is Number -> DiscordNumber(setting as Setting<Number>)
            else -> null //Should be unreachable when all setting types are implemented
        } ?: return

        if (setting.parentSetting != null) {
            shownSettings.add(
                shownSettings.indexOfFirst { it.dSetting == setting.parentSetting } + 1,
                toAdd
            ) //Add right after index of parentSetting
        } else {
            shownSettings.add(toAdd)
        }
    }

    private fun shouldShow(setting: Setting<*>): Boolean {
        var shouldShow = setting.isVisible()
        if (shouldShow && setting.parentSetting != null) {
            shouldShow = setting.parentSetting!!.isVisible()
        }
        return shouldShow
    }

}