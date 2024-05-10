package com.paragon.impl.module.client

import com.paragon.Paragon
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.IgnoredByNotifications
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import com.paragon.impl.ui.configuration.GuiImplementation
import com.paragon.impl.ui.configuration.camper.CamperCheatGUI
import com.paragon.util.mc
import me.surge.animation.Easing
import org.lwjgl.input.Keyboard

/**
 * @author SooStrator1136, Surge
 */
@IgnoredByNotifications
object ClickGUI : Module("ClickGUI", Category.CLIENT, "The ClickGUI of the client", Bind(Keyboard.KEY_RSHIFT, Bind.Device.KEYBOARD)) {

    @JvmStatic
    val style: Setting<Style> = Setting("Style", Style.CAMPER_CHEAT) describedBy "The style of the ClickGUI"

    // Windows settings
    @JvmStatic
    val gradient = Setting("Gradient", true) describedBy "Whether the windows should have a gradient" subOf style visibleWhen { style.value == Style.WINDOWS_98 }

    // Panel settings
    @JvmStatic
    val outline = Setting("Outline", false) describedBy "Outline the category panels" subOf style visibleWhen { style.value == Style.PANEL }

    // Shared settings
    @JvmStatic
    val animationSpeed = Setting("AnimationSpeed", 200f, 0f, 1000f, 10f) describedBy "How fast animations are"

    @JvmStatic
    val easing = Setting("Easing", Easing.EXPO_IN_OUT) describedBy "The easing of the animations" excludes Easing.BACK_IN

    @JvmStatic
    val darkenBackground = Setting("DarkenBackground", true) describedBy "Whether or not to darken the background"

    @JvmStatic
    val pause = Setting("Pause Game", false) describedBy "Pause the game whilst in the GUI"

    @JvmStatic
    val tooltips = Setting("Tooltips", true) describedBy "Render tooltips on the taskbar"

    val blur = Setting("WindowBlur", true) describedBy "Whether the windows have a blur"

    val intensity = Setting("Intensity", 10f, 1f, 20f, 1f) describedBy "The intensity of the blur" subOf blur

    val taskbarAlignment = Setting("TaskbarAlignment", TaskbarAlignment.LEFT) describedBy "Where the taskbar is positioned"
    val forceTaskbarVisibility = Setting("ForceTaskbarVisibility", true) describedBy "Whether to constantly show the taskbar"

    fun getGUI(): GuiImplementation = when (style.value) {
        Style.PANEL -> Paragon.INSTANCE.panelGUI
        Style.WINDOWS_98 -> Paragon.INSTANCE.windows98GUI
        Style.DISCORD -> Paragon.INSTANCE.discordGUI
        Style.PHOBOS -> Paragon.INSTANCE.phobosGUI
        Style.CAMPER_CHEAT -> CamperCheatGUI()
    }

    override fun onEnable() {
        mc.displayGuiScreen(Paragon.INSTANCE.configurationGUI)
        toggle()
    }

    enum class Style {
        /**
         * Original Paragon GUI
         */
        PANEL,

        /**
         * Windows 98 themed GUI
         */
        WINDOWS_98,

        /**
         * Discord styled GUI
         */
        DISCORD,

        /**
         * Phobos styled GUI
         */
        PHOBOS,

        /**
         * RusherHack styled GUI
         */
        CAMPER_CHEAT
    }

    enum class Icon {
        /**
         * No icon
         */
        NONE,

        /**
         * Just the icon
         */
        PLAIN,

        /**
         * Icon with a background
         */
        BACKGROUND
    }

    enum class TaskbarAlignment {
        BOTTOM,
        LEFT,
        TOP,
        RIGHT
    }

}