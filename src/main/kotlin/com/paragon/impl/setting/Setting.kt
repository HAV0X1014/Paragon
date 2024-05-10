package com.paragon.impl.setting

import com.paragon.Paragon
import com.paragon.impl.event.client.SettingUpdateEvent
import com.paragon.impl.module.annotation.Constant
import com.paragon.impl.module.client.Colours
import com.paragon.util.render.ColourUtil
import com.paragon.util.render.ColourUtil.integrateAlpha
import java.awt.Color

class Setting<T>(val name: String, value: T, val min: T = value, val max: T = value, val incrementation: T = value) {

    val subsettings = ArrayList<Setting<*>>()

    private val constant = this.javaClass.isAnnotationPresent(Constant::class.java)
    private val exclusions = arrayListOf<T>()

    /**
     * The description of the setting.
     */
    var description = ""
        private set

    /**
     * The current value held by the setting.
     */
    var value: T = value
        private set
        get() {
            if (field is Color) {
                if (isSync && this !== Colours.mainColour) {
                    return Colours.mainColour.value as T
                }

                if (isRainbow) {
                    return Color(
                        ColourUtil.getRainbow(
                            rainbowSpeed, rainbowSaturation / 100, 0
                        )
                    ).integrateAlpha((field as Color).alpha.toFloat()) as T
                }

                return (field as Color) as T
            }

            return field
        }

    // For mode settings
    var index = -1

    // For colour settings
    var isRainbow = false
    var rainbowSpeed = 4f
    var rainbowSaturation = 100f
    var isSync = false

    // Subsettings
    var parentSetting: Setting<*>? = null
        private set

    // GUI Visibility
    private var isVisible = { true }

    /**
     * Sets the description of the setting.
     *
     * @param description the description of the setting.
     * @return this setting
     */
    private fun setDescription(description: String): Setting<T> {
        this.description = description
        return this
    }

    /**
     * Sets the value of the setting.
     *
     * @param value the value of the setting.
     */
    fun setValue(value: T) {
        if (constant) {
            return
        }

        if (value != this.value) {
            Paragon.INSTANCE.eventBus.post(SettingUpdateEvent(this))
        }

        if (value is Enum<*>) {
            index = nextIndex
        }

        this.value = value

        if (value is Enum<*> && exclusions.contains(value)) {
            setValue(nextMode)
        }
    }

    /**
     * Sets the value without updating anything else
     *
     * @param value the value of the setting.
     */
    fun setValueRaw(value: T) {
        if (constant) {
            return
        }

        this.value = value
    }

    /**
     * Sets the parent setting of the setting.
     *
     * @param parentSetting the parent setting of the setting.
     * @return this setting
     */
    private fun setParentSetting(parentSetting: Setting<*>?): Setting<T> {
        this.parentSetting = parentSetting
        this.parentSetting!!.subsettings.add(this)
        return this
    }

    /**
     * Gets the visibility of the setting.
     *
     * @return the visibility of the setting.
     */
    fun isVisible() = isVisible.invoke()

    /**
     * Sets the visibility of the setting.
     *
     * @param isVisible the visibility of the setting.
     * @return this setting
     */
    private fun setVisibility(isVisible: () -> Boolean): Setting<T> {
        this.isVisible = isVisible
        return this
    }

    private fun addExclusion(exclusion: T): Setting<T> {
        this.exclusions.add(exclusion)
        return this
    }

    /**
     * Gets the next mode of the setting.
     *
     * @return the next mode of the setting.
     * @author linustouchtips
     */
    val nextMode: T
        get() {
            val enum = value as Enum<*>

            return java.lang.Enum.valueOf(
                enum::class.java,
                enum.javaClass.enumConstants.map { it.name }[nextIndex]
            ) as T
        }

    private val nextIndex: Int
        get() {
            val enum = value as Enum<*>

            return if (index + 1 > enum.javaClass.enumConstants.map { it.name }.size - 1) 0 else index + 1
        }

    infix fun describedBy(description: String) = setDescription(description)
    infix fun visibleWhen(isVisible: () -> Boolean) = setVisibility(isVisible)
    infix fun subOf(parent: Setting<*>?) = setParentSetting(parent)
    infix fun excludes(exclusion: T) = addExclusion(exclusion)

}