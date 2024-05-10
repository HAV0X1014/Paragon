package com.paragon.impl.module.client

import com.paragon.bus.listener.Listener
import com.paragon.impl.event.render.gui.GuiUpdateEvent
import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.annotation.Constant
import com.paragon.impl.module.annotation.NotVisibleByDefault
import com.paragon.impl.ui.menu.ParagonMenu
import com.paragon.util.mc
import net.minecraft.client.gui.GuiMainMenu

@NotVisibleByDefault
object MainMenu : Module("MainMenu", Category.CLIENT, "Use the client's custom main menu") {

    @Listener
    fun onGuiUpdate(event: GuiUpdateEvent) {
        if (event.screen is GuiMainMenu) {
            mc.displayGuiScreen(ParagonMenu())
            event.cancel()
        }
    }

}