package com.paragon.impl.module

import com.paragon.Paragon
import com.paragon.impl.event.client.ModuleToggleEvent
import com.paragon.impl.module.annotation.Aliases
import com.paragon.impl.module.annotation.Constant
import com.paragon.impl.module.annotation.IgnoredByNotifications
import com.paragon.impl.module.annotation.NotVisibleByDefault
import com.paragon.impl.module.hud.impl.ArrayListHUD
import com.paragon.impl.setting.Bind
import com.paragon.impl.setting.Setting
import me.surge.animation.Animation
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard

open class Module(val name: String, val category: Category, val description: String) {

    // Whether the module is visible in the Array List or not
    private val visible = Setting(
        "Visible",
        run {
            !(javaClass.isAnnotationPresent(NotVisibleByDefault::class.java) || category == Category.HUD)
        }
    ) describedBy "Whether the module is visible in the array list or not"

    val bind = Setting("Bind", Bind(Keyboard.KEY_NONE, Bind.Device.KEYBOARD)) describedBy "The keybind of the module"

    // Whether the module is constantly enabled or not
    private val isConstant = javaClass.isAnnotationPresent(Constant::class.java)

    // Whether the module is ignored by notifications
    val isIgnored = javaClass.isAnnotationPresent(IgnoredByNotifications::class.java)

    // List of search aliases
    val aliases = if (javaClass.isAnnotationPresent(Aliases::class.java)) {
        javaClass.getAnnotation(Aliases::class.java).aliases
    } else {
        arrayOf()
    }

    // Module Settings
    val settings: MutableList<Setting<*>> = ArrayList()

    // Arraylist animation
    val animation = Animation({ ArrayListHUD.animationSpeed.value }, false) { ArrayListHUD.easing.value }

    /**
     * Used to check if a module is enabled or not
     *
     * @see isActive
     */
    var isEnabled = false

    init {
        if (isConstant) {
            isEnabled = true

            // Register events
            MinecraftForge.EVENT_BUS.register(this)
            Paragon.INSTANCE.eventBus.register(this)
        }
    }

    constructor(name: String, category: Category, description: String, bind: Bind) : this(name, category, description) {
        this.bind.setValue(bind)
    }

    /**
     * Adds all the settings of a module to [settings] using reflection.
     */
    fun reflectSettings() {
        javaClass.declaredFields.filter { Setting::class.java.isAssignableFrom(it.type) }.forEach {
            it.isAccessible = true
            try {
                val setting = it[this] as Setting<*>
                if (setting.parentSetting == null) {
                    settings.add(setting)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        settings.add(visible)
        settings.add(bind)
    }

    open fun onEnable() {}
    open fun onDisable() {}
    open fun onTick() {}
    open fun onRender2D() {}
    open fun onRender3D() {}

    open fun getData() = ""

    open fun isActive() = isEnabled

    /**
     * Toggles the module
     *
     * @see onEnable
     * @see onDisable
     */
    fun toggle() {
        // We don't want to toggle if the module is constant
        if (isConstant) {
            return
        }

        isEnabled = !isEnabled

        Paragon.INSTANCE.eventBus.post(ModuleToggleEvent(this))

        if (isEnabled) {
            // Register events
            MinecraftForge.EVENT_BUS.register(this)
            com.paragon.Paragon.INSTANCE.eventBus.register(this)
            animation.state = true

            // Call onEnable
            onEnable()
        } else {
            // Unregister events
            MinecraftForge.EVENT_BUS.unregister(this)
            com.paragon.Paragon.INSTANCE.eventBus.unregister(this)
            animation.state = false

            // Call onDisable
            onDisable()
        }
    }

    /**
     * Checks if a given [search] term should show this module
     * @param search The given search string
     */
    fun isValidSearch(search: String): Boolean {
        return name.contains(search, true) || aliases.any { it.contains(search, true) }
    }

    /**
     * Gets the module's visibility
     *
     * @return The module's visibility
     */
    fun isVisible() = visible.value

    /**
     * Sets the module's visibility
     *
     * @param visible The module's new visibility
     */
    fun setVisible(visible: Boolean) {
        this.visible.setValue(visible)
    }

}