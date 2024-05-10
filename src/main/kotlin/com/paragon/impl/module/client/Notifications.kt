package com.paragon.impl.module.client

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.setting.Setting

/**
 * @author Surge
 * @since 25/12/2022
 */
object Notifications : Module("Notifications", Category.CLIENT, "Changes how notifications are shown to you") {

    val display = Setting("Display", Display.POP_OUT) describedBy "How notifications are shown"

    enum class Display {
        /**
         * Pop-out notifications, i.e. at the side
         */
        POP_OUT,

        /**
         * Chat notifications
         */
        CHAT
    }

}