package com.paragon.impl.module.client

import com.paragon.impl.module.Category
import com.paragon.impl.module.Module
import com.paragon.impl.module.hud.EditorGUI
import com.paragon.impl.setting.Setting
import com.paragon.util.mc

/**
 * @author Surge
 * @since 24/12/2022
 */
object Editor : Module("Editor", Category.CLIENT, "Allows you to edit the positions of HUD modules") {

    val snap = Setting("Snap", 5f, 1f, 20f, 1f) describedBy "The snap spacing"

    override fun onEnable() {
        mc.displayGuiScreen(EditorGUI())
        toggle()
    }

}