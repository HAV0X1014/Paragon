package com.paragon.impl.module.movement

import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting
import com.paragon.util.player.PlayerUtil
import com.paragon.impl.module.Category
import com.paragon.util.mc
import com.paragon.util.string.StringUtil

/**
 * @author Surge, SooStrator1136
 */
object Sprint : Module("Sprint", Category.MOVEMENT, "Automatically sprint") {

    private val mode = Setting(
        "Mode", Mode.LEGIT
    ) describedBy "The mode to sprint in"
    private val onlyWhenMoving = Setting(
        "WhenMoving", true
    ) describedBy "Only omni sprint when actually moving" visibleWhen { mode.value === Mode.OMNI }

    override fun onDisable() {
        // Stop sprinting when we disable
        mc.player?.isSprinting = false
    }

    override fun onTick() {
        mc.player?.let {
            when (mode.value) {
                Mode.OMNI -> {
                    if (onlyWhenMoving.value && !PlayerUtil.isMoving) {
                        return
                    }

                    it.isSprinting = true
                }

                Mode.LEGIT -> it.isSprinting = it.movementInput.moveForward > 0
            }
        }
    }

    override fun getData() = StringUtil.getFormattedText(mode.value)

    enum class Mode {

        /**
         * Only sprint if you are walking forwards
         */
        LEGIT,

        /**
         * Always sprint
         */
        OMNI
    }

}