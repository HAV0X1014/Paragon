package com.paragon.impl.module.misc

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.player.ClickBothMouseButtonsEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module

/**
 * @author Surge
 * @since 07/12/2022
 */
object MultiTask : Module("MultiTask", Category.MISC, "Allows you to eat and mine at the same time") {

    @Listener
    fun onClickBothMouseButtons(event: ClickBothMouseButtonsEvent) {
        event.cancel()
    }

}